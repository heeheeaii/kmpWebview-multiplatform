package com.hee.sample

import com.multiplatform.webview.setting.WebSettings
import java.util.UUID

actual fun setupPlatformWebSettings(webSettings: WebSettings) {
    webSettings.androidWebSettings.apply {
        domStorageEnabled = true
        allowFileAccess   = true
        useWideViewPort   = true
    }
}

actual fun randomUUID(): String = UUID.randomUUID().toString()
