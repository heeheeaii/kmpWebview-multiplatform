package com.hee.sample

import android.webkit.CookieManager
import com.multiplatform.webview.setting.WebSettings
import com.multiplatform.webview.web.NativeWebView
import java.util.UUID

actual fun setupPlatformWebSettings(nativeWebView: NativeWebView, webSettings: WebSettings) {
    webSettings.androidWebSettings.apply {
        domStorageEnabled = true
        allowFileAccess = true
    }
    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(nativeWebView, true)
    }
}

actual fun randomUUID(): String = UUID.randomUUID().toString()
