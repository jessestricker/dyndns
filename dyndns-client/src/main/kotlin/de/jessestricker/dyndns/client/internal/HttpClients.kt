package de.jessestricker.dyndns.client.internal

import io.ktor.client.request.*
import io.ktor.http.*

internal fun HttpRequestBuilder.pathSegments(vararg segments: String) {
    url.appendPathSegments(*segments, encodeSlash = true)
}
