package io.github.vyfor.klipy

import io.github.vyfor.klipy.model.AdItem
import io.github.vyfor.klipy.model.ClipItem
import io.github.vyfor.klipy.model.EmojiMeta
import io.github.vyfor.klipy.model.EmojiStatus
import io.github.vyfor.klipy.model.Envelope
import io.github.vyfor.klipy.model.GenerationStatus
import io.github.vyfor.klipy.model.MediaItem
import io.github.vyfor.klipy.model.Page
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private const val BASE_URL = "https://api.klipy.com/api/v1"

/** Async client for the [KLIPY API](https://klipy.com).
 *
 * ```kotlin
 * val klipy = KlipyClient("YOUR_APP_KEY")
 * val page = klipy.gifs().trending().perPage(20).send()
 * page.contentItems().forEach { println(it.slug) }
 * ```
 */
class KlipyClient(
    private val appKey: String,
    private val httpClient: HttpClient = defaultHttpClient(),
) : AutoCloseable {
    /** GIF endpoints. */
    fun gifs() = Endpoints(this, "gifs", MediaItem.serializer())

    /** Sticker endpoints. */
    fun stickers() = Endpoints(this, "stickers", MediaItem.serializer())

    /** Clip endpoints. Note: the clips library is not yet fully MPA-rated. */
    fun clips() = Endpoints(this, "clips", ClipItem.serializer())

    /** Meme endpoints. */
    fun memes() = Endpoints(this, "static-memes", MediaItem.serializer())

    /** AI Emoji endpoints. */
    fun emojis() = Endpoints(this, "emojis", MediaItem.serializer())

    /** Generate an AI Emoji from a text [prompt] (max 300 chars). Returns a generation ID
     * immediately; the emoji is processed asynchronously. Poll [emojiStatus] or provide a
     * [callbackUrl] to receive the result via webhook. */
    suspend fun generateEmoji(
        prompt: String,
        callbackUrl: String? = null,
    ): String {
        val body =
            buildJsonObject {
                put("prompt", prompt)
                callbackUrl?.let { put("callback_url", it) }
            }
        val env: Envelope<JsonObject> =
            httpClient
                .post(url("emojis/generate")) {
                    contentType(ContentType.Application.Json)
                    setBody(body.toString())
                }.body()
        return env.data!!
            .getValue("id")
            .jsonPrimitive.content
    }

    /** Check the status of an AI Emoji generation request. Status is one of
     * `processing`, `success`, or `failed`. */
    suspend fun emojiStatus(id: String): EmojiStatus = get("emojis/generated/$id")

    /** Related search terms for a query. `limit`: 1-50, default 10. */
    suspend fun searchSuggestions(
        q: String,
        limit: Int = 10,
    ): List<String> = get("search-suggestions/$q") { parameter("limit", limit) }

    /** Completed search terms for a partial query. `limit`: 1-50, default 10. */
    suspend fun autocomplete(
        q: String,
        limit: Int = 10,
    ): List<String> = get("autocomplete/$q") { parameter("limit", limit) }

    internal suspend inline fun <reified T> get(
        path: String,
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T {
        val response = httpClient.get(url(path), block)
        checkStatus(response)
        val env: Envelope<T> = response.body()
        return env.data ?: error("KLIPY returned result=false or missing data")
    }

    internal suspend fun <T> getPage(
        path: String,
        serializer: KSerializer<T>,
        block: HttpRequestBuilder.() -> Unit = {},
    ): Page<T> {
        val response = httpClient.get(url(path), block)
        checkStatus(response)
        val env = json.decodeFromString<Envelope<JsonObject>>(response.bodyAsText())
        val obj = env.data ?: error("KLIPY returned result=false or missing data")
        return Page(
            data = decodePageItems(obj["data"]!!.jsonArray.toList(), serializer, json),
            currentPage = obj["current_page"]?.jsonPrimitive?.intOrNull,
            perPage = obj["per_page"]?.jsonPrimitive?.intOrNull,
            hasNext = obj["has_next"]?.jsonPrimitive?.booleanOrNull ?: false,
            meta = obj["meta"]?.let { json.decodeFromJsonElement(EmojiMeta.serializer(), it) },
        )
    }

    internal suspend fun ack(block: suspend HttpClient.() -> HttpResponse) {
        val response = httpClient.block()
        checkStatus(response)
        val obj = json.decodeFromString<JsonObject>(response.bodyAsText())
        check(obj["result"]?.jsonPrimitive?.booleanOrNull == true) { "KLIPY returned result=false" }
    }

    internal fun url(path: String) = "$BASE_URL/$appKey/$path"

    override fun close() = httpClient.close()

    /** Builder for [KlipyClient]. */
    class Builder(
        private val appKey: String,
    ) {
        private var userAgent: String? = null
        private var httpClient: HttpClient? = null

        /** Set a default `User-Agent` for all requests. Required for ad delivery. */
        fun userAgent(ua: String) = apply { userAgent = ua }

        /** Use a pre-configured [HttpClient]. Takes precedence over [userAgent]. */
        fun httpClient(client: HttpClient) = apply { httpClient = client }

        fun build() = KlipyClient(appKey, httpClient ?: defaultHttpClient(userAgent))
    }

    companion object {
        operator fun invoke(
            appKey: String,
            block: Builder.() -> Unit,
        ) = Builder(appKey).apply(block).build()
    }
}

internal val json =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

private fun defaultHttpClient(userAgent: String? = null) =
    HttpClient {
        install(ContentNegotiation) { json(json) }
        install(DefaultRequest) {
            userAgent?.let { header(HttpHeaders.UserAgent, it) }
        }
    }

private fun checkStatus(response: HttpResponse) {
    check(response.status.isSuccess()) { "KLIPY request failed: ${response.status}" }
}
