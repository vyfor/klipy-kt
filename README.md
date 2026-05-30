# klipy

Async Kotlin Multiplatform client for the [KLIPY API](https://docs.klipy.com).

This is an unofficial library and is not affiliated with Klipy in any way.

## Installation

<a href="https://central.sonatype.com/artifact/io.github.vyfor/klipy"><img src="https://img.shields.io/maven-central/v/io.github.vyfor/klipy?label=klipy"></a>
<a href="https://central.sonatype.com/artifact/io.ktor/ktor"><img src="https://img.shields.io/maven-central/v/io.ktor/ktor?label=ktor"></a>

```kotlin
dependencies {
    implementation("io.github.vyfor:klipy:$version")
    /* required */
    implementation("io.ktor:ktor-client-$engine:$version")
}
```

For the list of supported engines, see [Ktor Client Engines](https://ktor.io/docs/client-engines.html#platforms).

## Usage

```kotlin
val klipy = KlipyClient("YOUR_APP_KEY")

// trending GIFs
val page = klipy.gifs().trending().perPage(20).send()
page.contentItems().forEach { println("${it.slug} - ${it.kind}") }

// search
val page = klipy.stickers().search("happy").contentFilter(ContentFilter.Medium).send()

// fetch by slug
val page = klipy.clips().items("they-love-me-spider-man")

// log a share
klipy.gifs().share("hello-hi-662").customerId("user-uuid").send()

// search suggestions
val suggestions = klipy.searchSuggestions("hel", limit = 10)
```

## Ads

```kotlin
val klipy = KlipyClient("YOUR_APP_KEY") {
    userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148")
}

val page = klipy.gifs().trending()
    .adParams(AdParams(
        customerId = "user-uuid",
        minWidth = 50,
        maxWidth = 400,
        minHeight = 50,
        maxHeight = 250,
        os = "ios",
    ))
    .send()

for (item in page.data) {
    when (item) {
        is Item.Content -> { /* display item.value */ }
        is Item.Ad -> { /* render item.value.content (either HTML or iFrame) */ }
    }
}
```

## Custom client

```kotlin
val klipy = KlipyClient("YOUR_APP_KEY") {
    userAgent("...")
}
```

Or bring your own `HttpClient`:

```kotlin
val klipy = KlipyClient("YOUR_APP_KEY") {
    httpClient(myClient)
}
```

## Licensing

Dual licensed under MIT OR Apache-2.0 at your discretion. See [LICENSE-MIT](LICENSE-MIT) and [LICENSE-APACHE](LICENSE-APACHE) for details.
