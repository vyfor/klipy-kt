package io.github.vyfor.klipy

import io.github.vyfor.klipy.model.AdItem
import io.github.vyfor.klipy.model.Categories
import io.github.vyfor.klipy.model.Item
import io.github.vyfor.klipy.model.Page
import io.github.vyfor.klipy.params.AdParams
import io.github.vyfor.klipy.params.ContentFilter
import io.github.vyfor.klipy.params.Format
import io.github.vyfor.klipy.params.ReportReason
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Unified endpoint builders for different content kinds. */
class Endpoints<T> internal constructor(
    private val client: KlipyClient,
    private val path: String,
    private val serializer: KSerializer<T>,
) {
    /** Trending items, updated throughout the day and localized by language and region. */
    fun trending() = ListBuilder(client, "$path/trending", serializer)

    /** Search by keyword. Results are ranked by relevance, popularity, and
     * language context with fuzzy matching support. */
    fun search(q: String) = ListBuilder(client, "$path/search", serializer).query("q", q)

    /** Items recently used by a specific user. Pass a stable [customerId] to fetch per-user history. */
    fun recent(customerId: String) = ListBuilder(client, "$path/recent/$customerId", serializer)

    /** Fetch specific items by slug. */
    suspend fun items(vararg slugs: String): Page<T> = items(slugs.toList())

    /** Fetch specific items by slug. */
    suspend fun items(slugs: Iterable<String>): Page<T> =
        client.getPage("$path/items", serializer) { parameter("slugs", slugs.joinToString(",")) }

    /** Remove an item from a user's Recent list. */
    suspend fun hideRecent(
        customerId: String,
        slug: String,
    ) = client.ack { delete("$path/recent/$customerId") { parameter("slug", slug) } }

    /** Log a share event for analytics and personalization. */
    fun share(slug: String) = ShareBuilder(client, "$path/share/$slug")

    /** Report an item flagged by a user. */
    fun report(
        slug: String,
        reason: ReportReason,
    ) = ReportBuilder(client, "$path/report/$slug", reason)

    /** Curated categories for this content kind. [locale] is `xx_YY` format (e.g. `"en_US"`). */
    suspend fun categories(locale: String): Categories = client.get("$path/categories") { parameter("locale", locale) }
}

/** Builder for paginated list endpoints (trending, search, recent). */
class ListBuilder<T> internal constructor(
    private val client: KlipyClient,
    private val path: String,
    private val serializer: KSerializer<T>,
) {
    private val params = mutableListOf<Pair<String, String>>()

    internal fun query(
        key: String,
        value: String,
    ) = apply { params += key to value }

    /** Page number (min 1, default 1). */
    fun page(page: Int) = apply { params += "page" to page.toString() }

    /** Items per page. */
    fun perPage(perPage: Int) = apply { params += "per_page" to perPage.toString() }

    /** Stable user identifier for personalization. */
    fun customerId(customerId: String) = apply { params += "customer_id" to customerId }

    /** ISO 3166 Alpha-2 country/language code (e.g. `"us"`, `"ru"`). */
    fun locale(locale: String) = apply { params += "locale" to locale }

    /** Content safety filter level. */
    fun contentFilter(filter: ContentFilter) = apply { params += "content_filter" to filter.value }

    /** Restrict results to specific formats only. */
    fun formatFilter(vararg formats: Format) =
        apply {
            params += "format_filter" to formats.joinToString(",") { it.value }
        }

    /** Attach ad request parameters. Requires a browser-like User-Agent set via
     * [KlipyClient.Builder.userAgent]. */
    fun adParams(adParams: AdParams) = apply { params += adParams.toQueryParams() }

    /** Execute the request. */
    suspend fun send(): Page<T> =
        client.getPage(path, serializer) {
            params.forEach { (k, v) -> parameter(k, v) }
        }
}

/** Builder for the share-trigger endpoint. */
class ShareBuilder internal constructor(
    private val client: KlipyClient,
    private val path: String,
) {
    private var customerId: String? = null
    private var query: String? = null

    /** Stable user identifier. */
    fun customerId(customerId: String) = apply { this.customerId = customerId }

    /** The search query that led to this share. Required for Search API shares. */
    fun query(q: String) = apply { this.query = q }

    /** Execute the request. */
    suspend fun send() =
        client.ack {
            post(path) {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("customer_id", customerId ?: "")
                        put("q", query ?: "")
                    }.toString(),
                )
            }
        }
}

/** Builder for the report endpoint. */
class ReportBuilder internal constructor(
    private val client: KlipyClient,
    private val path: String,
    private val reason: ReportReason,
) {
    private var customerId: String? = null

    /** Stable user identifier. */
    fun customerId(customerId: String) = apply { this.customerId = customerId }

    /** Execute the request. */
    suspend fun send() =
        client.ack {
            post(path) {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("customer_id", customerId ?: "")
                        put("reason", reason.value)
                    }.toString(),
                )
            }
        }
}

internal fun <T> decodePageItems(
    elements: List<JsonElement>,
    serializer: KSerializer<T>,
    json: Json,
): List<Item<T>> =
    elements.mapTo(ArrayList(elements.size)) { element ->
        val obj = element as? JsonObject
        if (obj?.get("type")?.jsonPrimitive?.content == "ad") {
            Item.Ad(json.decodeFromJsonElement(AdItem.serializer(), element))
        } else {
            Item.Content(json.decodeFromJsonElement(serializer, element))
        }
    }
