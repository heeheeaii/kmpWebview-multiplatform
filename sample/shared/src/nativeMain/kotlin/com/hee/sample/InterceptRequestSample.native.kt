package com.hee.sample

import com.multiplatform.webview.setting.WebSettings
import com.multiplatform.webview.web.NativeWebView

actual fun setupPlatformWebSettings(nativeWebView: NativeWebView, webSettings: WebSettings) {
}

actual fun randomUUID(): String {
    return ""
}
