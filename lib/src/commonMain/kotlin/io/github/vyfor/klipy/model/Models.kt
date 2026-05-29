package io.github.vyfor.klipy.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A single concrete media file. */
@Serializable
data class MediaFile(
    val url: String,
    val width: Int,
    val height: Int,
    val size: Long,
)

/** The set of formats available at a given size. */
@Serializable
data class Formats(
    val gif: MediaFile? = null,
    val webp: MediaFile? = null,
    val jpg: MediaFile? = null,
    val mp4: MediaFile? = null,
    val webm: MediaFile? = null,
    val png: MediaFile? = null,
)

/** Renditions of a media item across its available sizes. */
@Serializable
data class Sizes(
    val hd: Formats? = null,
    val md: Formats? = null,
    val sm: Formats? = null,
    val xs: Formats? = null,
)

/** A GIF, sticker, meme, or emoji item. */
@Serializable
data class MediaItem(
    val id: Long,
    val slug: String,
    val title: String,
    val file: Sizes,
    val tags: List<String> = emptyList(),
    @SerialName("type") val kind: String,
    @SerialName("blur_preview") val blurPreview: String? = null,
)

/** Width/height pair for a clip rendition. */
@Serializable
data class Dimensions(
    val width: Int,
    val height: Int,
)

/** Direct URLs for a clip's renditions. */
@Serializable
data class ClipFiles(
    val mp4: String? = null,
    val gif: String? = null,
    val webp: String? = null,
)

/** A movie/video clip item. */
@Serializable
data class ClipItem(
    val url: String,
    val title: String,
    val slug: String,
    val file: ClipFiles,
    @SerialName("file_meta") val fileMeta: Map<String, Dimensions> = emptyMap(),
    val tags: List<String> = emptyList(),
    @SerialName("type") val kind: String,
    @SerialName("blur_preview") val blurPreview: String? = null,
)

/** An advertisement object. If ads are enabled for your app, some API
 * responses may include an advertisement object alongside content objects.
 * When `type` is `"ad"`, the object is an advertisement. */
@Serializable
data class AdItem(
    /** HTML content to display (webview for mobile apps). */
    val content: String,
    val width: Int,
    val height: Int,
    @SerialName("type") val kind: String,
)

/** The generated emoji data. */
@Serializable
data class GeneratedEmoji(
    @SerialName("base64_encoded") val base64Encoded: String,
    @SerialName("mime_type") val mimeType: String,
)

/** Status of an AI Emoji generation request. */
@Serializable
enum class GenerationStatus {
    @SerialName("processing")
    Processing,

    @SerialName("success")
    Success,

    @SerialName("failed")
    Failed,
}

/** Status and result of an AI Emoji generation request. */
@Serializable
data class EmojiStatus(
    val id: String,
    val status: GenerationStatus,
    val result: GeneratedEmoji? = null,
)

/** A list entry: either content of type [T] or an advertisement. */
sealed class Item<out T> {
    /** Content item. */
    data class Content<T>(
        val value: T,
    ) : Item<T>()

    /** Advertisement item. */
    data class Ad(
        val value: AdItem,
    ) : Item<Nothing>()

    /** Returns the content item, or `null` if this entry is an advertisement. */
    fun content(): T? = (this as? Content)?.value

    /** Returns the advertisement, or `null` if this entry is content. */
    fun ad(): AdItem? = (this as? Ad)?.value
}

/** Metadata returned alongside AI Emoji list responses. */
@Serializable
data class EmojiMeta(
    /** Minimum display width for each emoji item, in pixels. */
    @SerialName("item_min_width") val itemMinWidth: Int? = null,
    /** Maximum percentage by which an ad may be resized. */
    @SerialName("ad_max_resize_percent") val adMaxResizePercent: Int? = null,
)

/** A paginated list of items. Pagination fields are absent for the `items`
 * endpoint. The [meta] field is present only on AI Emoji list responses. */
data class Page<T>(
    val data: List<Item<T>>,
    val currentPage: Int?,
    val perPage: Int?,
    val hasNext: Boolean,
    val meta: EmojiMeta?,
) {
    /** Iterate over content items only, skipping any advertisements. */
    fun contentItems(): Sequence<T> = data.asSequence().filterIsInstance<Item.Content<T>>().map { it.value }

    /** Iterate over advertisements only, skipping any content items. */
    fun adItems(): Sequence<AdItem> = data.asSequence().filterIsInstance<Item.Ad>().map { it.value }
}

/** A curated content category. */
@Serializable
data class Category(
    val category: String,
    val query: String,
    @SerialName("preview_url") val previewUrl: String? = null,
)

/** The payload returned by a `categories` request. */
@Serializable
data class Categories(
    val locale: String,
    val categories: List<Category>,
)

@Serializable
internal data class Envelope<T>(
    val result: Boolean,
    val data: T? = null,
)
