package com.hee.sample

import com.multiplatform.webview.setting.WebSettings
import com.multiplatform.webview.web.NativeWebView

actual fun randomUUID(): String {
    return ""
}

actual fun setupPlatformWebSettings(
    nativeWebView: NativeWebView,
    webSettings: WebSettings
) {
}
