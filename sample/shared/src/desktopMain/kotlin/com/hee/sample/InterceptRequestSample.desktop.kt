package com.hee.sample

import com.multiplatform.webview.setting.WebSettings
import java.util.UUID

/** Platform-specific tweaks live in expect/actual declarations. */
actual fun setupPlatformWebSettings(webSettings: WebSettings) {
}

actual fun randomUUID(): String = UUID.randomUUID().toString()
