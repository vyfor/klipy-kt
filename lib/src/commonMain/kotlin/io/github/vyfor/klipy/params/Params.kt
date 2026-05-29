package io.github.vyfor.klipy.params

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Specify the content safety filter level. */
@Serializable
enum class ContentFilter {
    @SerialName("off")
    Off,

    @SerialName("low")
    Low,

    @SerialName("medium")
    Medium,

    @SerialName("high")
    High,

    ;

    internal val value get() = name.lowercase()
}

/** A desired format. Possible values: gif, webp, jpg, mp4, webm (stickers also use png). */
@Serializable
enum class Format {
    @SerialName("gif")
    Gif,

    @SerialName("webp")
    Webp,

    @SerialName("jpg")
    Jpg,

    @SerialName("mp4")
    Mp4,

    @SerialName("webm")
    Webm,

    @SerialName("png")
    Png,

    ;

    internal val value get() = name.lowercase()
}

/** The reason for reporting the content, providing context for KLIPY's review process. */
@Serializable
enum class ReportReason(
    internal val value: String,
) {
    /** Nudity or sexually explicit content. */
    @SerialName("nudity")
    Nudity("nudity"),

    /** Graphic violence or violent behavior. */
    @SerialName("violence")
    Violence("violence"),

    /** Racist, homophobic, or hateful content. */
    @SerialName("hate_speech")
    HateSpeech("hate_speech"),

    /** Bullying, personal attacks, or targeted harassment. */
    @SerialName("harassment")
    Harassment("harassment"),

    /** Repetitive, irrelevant, or misleading content. */
    @SerialName("spam")
    Spam("spam"),

    /** False claims, misleading text, or manipulated media. */
    @SerialName("misinformation")
    Misinformation("misinformation"),

    /** Content believed to infringe on intellectual property rights. */
    @SerialName("copyright")
    Copyright("copyright"),

    /** Generally offensive or culturally inappropriate material. */
    @SerialName("offensive")
    Offensive("offensive"),

    /** Content that promotes or depicts illegal activity. */
    @SerialName("illegal")
    Illegal("illegal"),

    /** Content doesn't load, is corrupted, or is unplayable. */
    @SerialName("broken")
    Broken("broken"),

    /** Extremely low resolution or unreadable content. */
    @SerialName("low_quality")
    LowQuality("low_quality"),

    /** Content doesn't match the tag/query or is miscategorized. */
    @SerialName("not_relevant")
    NotRelevant("not_relevant"),

    /** Fake identity, misleading branding, or impersonation. */
    @SerialName("impersonation")
    Impersonation("impersonation"),

    /** Other issues not listed above. Free-text description recommended. */
    @SerialName("other")
    Other("other"),
}

/** Ad request parameters for list endpoints.
 * Required options: `customer_id`, `min_width`, `max_width`, `min_height`, `max_height`. */
data class AdParams(
    /** Unique user ID. Required for ad delivery. */
    val customerId: String? = null,
    /** Minimum ad width in pixels (recommended: 50). */
    val minWidth: Int? = null,
    /** Maximum ad width in pixels (recommended: device width). */
    val maxWidth: Int? = null,
    /** Minimum ad height in pixels (recommended: 50). */
    val minHeight: Int? = null,
    /** Maximum ad height in pixels (recommended: 250). */
    val maxHeight: Int? = null,
    /** Ad position in the response array (0-based). Defaults to client setting
     * or a random value between 0 and 2. */
    val position: Int? = null,
    /** `true` to receive an iframe URL instead of HTML content. */
    val iframe: Boolean? = null,
    /** App version (e.g. `"10.9.3"`). */
    val appVersion: String? = null,
    /** OS name (e.g. `"ios"`, `"android"`). */
    val os: String? = null,
    /** OS version (e.g. `"18.0"`). */
    val osVersion: String? = null,
    /** Hardware version (e.g. `"15"`). */
    val hardwareVersion: String? = null,
    /** Device make. */
    val make: String? = null,
    /** Device model. */
    val model: String? = null,
    /** Advertiser ID: IDFA (iOS), GPID (Android), unhashed. */
    val ifa: String? = null,
    /** Physical screen height in pixels. */
    val deviceH: Int? = null,
    /** Physical screen width in pixels. */
    val deviceW: Int? = null,
    /** Pixels per inch. */
    val ppi: Int? = null,
    /** Physical-to-device-independent pixel ratio. */
    val pxRatio: Float? = null,
    /** Language, ISO-639-1-alpha-2 (e.g. `"EN"`). */
    val language: String? = null,
    /** Carrier or ISP. */
    val carrier: String? = null,
    /** MCC-MNC code (e.g. `"310-005"`). */
    val mccMnc: String? = null,
    /** Connection type: 0=unknown, 1=ethernet, 2=WiFi, 3=cellular(unknown), 4=2G, 5=3G, 6=4G, 7=5G. */
    val connectionType: Int? = null,
    /** IMEI hashed via SHA1. */
    val didSha1: String? = null,
    /** IMEI hashed via MD5. */
    val didMd5: String? = null,
    /** Platform device ID hashed via SHA1. */
    val dpidSha1: String? = null,
    /** Platform device ID hashed via MD5. */
    val dpidMd5: String? = null,
    /** MAC address hashed via SHA1. */
    val macSha1: String? = null,
    /** MAC address hashed via MD5. */
    val macMd5: String? = null,
    /** User year of birth (4-digit). */
    val yearOfBirth: Int? = null,
    /** User gender: `"M"`, `"F"`, or `"O"`. */
    val gender: String? = null,
) {
    internal fun toQueryParams(): List<Pair<String, String>> =
        buildList {
            customerId?.let { add("customer_id" to it) }
            minWidth?.let { add("ad-min-width" to it.toString()) }
            maxWidth?.let { add("ad-max-width" to it.toString()) }
            minHeight?.let { add("ad-min-height" to it.toString()) }
            maxHeight?.let { add("ad-max-height" to it.toString()) }
            position?.let { add("ad-position" to it.toString()) }
            iframe?.let { add("ad-iframe" to if (it) "1" else "0") }
            appVersion?.let { add("ad-app-version" to it) }
            os?.let { add("ad-os" to it) }
            osVersion?.let { add("ad-osv" to it) }
            hardwareVersion?.let { add("ad-hwv" to it) }
            make?.let { add("ad-make" to it) }
            model?.let { add("ad-model" to it) }
            ifa?.let { add("ad-ifa" to it) }
            deviceH?.let { add("ad-device-h" to it.toString()) }
            deviceW?.let { add("ad-device-w" to it.toString()) }
            ppi?.let { add("ad-ppi" to it.toString()) }
            pxRatio?.let { add("ad-pxratio" to it.toString()) }
            language?.let { add("ad-language" to it) }
            carrier?.let { add("ad-carrier" to it) }
            mccMnc?.let { add("ad-mccmnc" to it) }
            connectionType?.let { add("ad-connection-type" to it.toString()) }
            didSha1?.let { add("ad-didsha1" to it) }
            didMd5?.let { add("ad-didmd5" to it) }
            dpidSha1?.let { add("ad-dpidsha1" to it) }
            dpidMd5?.let { add("ad-dpidmd5" to it) }
            macSha1?.let { add("ad-macsha1" to it) }
            macMd5?.let { add("ad-macmd5" to it) }
            yearOfBirth?.let { add("ad-yob" to it.toString()) }
            gender?.let { add("ad-gender" to it) }
        }
}
