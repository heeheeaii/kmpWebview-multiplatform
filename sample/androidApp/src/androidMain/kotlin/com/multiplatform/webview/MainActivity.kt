package com.multiplatform.webview

import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.hee.sample.MainWebView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainWebView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CookieManager.getInstance().flush()
        // Writes the WebView's cookies from memory to persistent
        // storage (disk). Although in newer versions of Android,
        // cookies are automatically synced.Manually calling flush()
        // is still a recommended practice.
    }
}
