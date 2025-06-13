package com.hee.sample.config

import com.multiplatform.webview.setting.WebSettings
import com.multiplatform.webview.util.KLogSeverity

fun WebSettings.applyDefault() {
    logSeverity = KLogSeverity.Debug
    customUserAgentString =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    isJavaScriptEnabled = true
    supportZoom = true
    allowFileAccessFromFileURLs = true
    allowUniversalAccessFromFileURLs = true
}
