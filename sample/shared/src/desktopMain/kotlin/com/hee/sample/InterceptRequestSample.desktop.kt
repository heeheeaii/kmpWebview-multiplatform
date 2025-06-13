package com.hee.sample

import com.multiplatform.webview.setting.WebSettings
import com.multiplatform.webview.web.NativeWebView
import java.util.UUID

/** Platform-specific tweaks live in expect/actual declarations. */
actual fun setupPlatformWebSettings(nativeWebView: NativeWebView, webSettings: WebSettings) {
    webSettings.desktopWebSettings.apply {
        // JavaFX WebView (or JCEF) has cookies and localStorage enabled by default.    }
    }
}

actual fun randomUUID(): String = UUID.randomUUID().toString()
