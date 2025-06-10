package com.hee.sample.data

import android.net.Uri

actual class CommonUri(private val androidUri: Uri) {
    actual val host: String?
        get() = androidUri.host

    actual companion object {
        actual fun parse(uriString: String): CommonUri {
            return CommonUri(Uri.parse(uriString))
        }
    }
}
