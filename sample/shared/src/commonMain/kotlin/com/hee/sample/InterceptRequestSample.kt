package com.hee.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.hee.sample.ui.BrowserBody
import com.hee.sample.ui.BrowserTopBar
import com.hee.sample.ui.TabBar
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.setting.WebSettings
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import kotlinx.coroutines.delay

data class TabInfo(
    val id: String = randomUUID(),
    val initialUrl: String? = null,
    val initialHtml: String? = null,
    var title: MutableState<String> = mutableStateOf("Home")
)

@Composable
fun InterceptRequestSample(navController: NavHostController? = null) {
    var darkTheme by rememberSaveable { mutableStateOf(true) }
    var forceDark by rememberSaveable { mutableStateOf(false) }
    var sidebarVisible by rememberSaveable { mutableStateOf(true) }

    val tabs = remember { mutableStateListOf<TabInfo>() }
    var activeTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabStateMap = remember { mutableStateMapOf<String, Pair<WebViewState, WebViewNavigator>>() }

    DisposableEffect(Unit) {
        onDispose {
            // Cleanup logic if needed in the future
        }
    }

    LaunchedEffect(Unit) {
        if (tabs.isEmpty()) {
            tabs.add(TabInfo(initialHtml = BrowserConfig.INITIAL_HTML))
        }
    }

    val activeTabInfo = tabs.getOrNull(activeTabIndex)
    val activeTabStateAndNav = activeTabInfo?.id?.let { tabStateMap[it] }
    val activeTabState = activeTabStateAndNav?.first
    val activeNavigator = activeTabStateAndNav?.second

    val colors = if (darkTheme) darkColors() else lightColors()

    MaterialTheme(colors = colors) {
        Column {
            BrowserTopBar(
                navigator = activeNavigator,
                onBack = {
                    if (activeNavigator?.canGoBack == true) activeNavigator.navigateBack() else navController?.popBackStack()
                },
                darkTheme = darkTheme,
                forceDark = forceDark,
                sidebarVisible = sidebarVisible,
                onToggleDarkTheme = { darkTheme = !darkTheme },
                onToggleForceDark = { forceDark = !forceDark },
                onToggleSidebar = { sidebarVisible = !sidebarVisible }
            )

            TabBar(
                tabs = tabs,
                activeTabIndex = activeTabIndex,
                onTabSelected = { index -> activeTabIndex = index },
                onTabClosed = { tabInfo ->
                    tabStateMap.remove(tabInfo.id)
                    tabs.remove(tabInfo)
                    if (activeTabIndex >= tabs.size && tabs.isNotEmpty()) {
                        activeTabIndex = tabs.size - 1
                    } else if (tabs.isEmpty()) {
                        tabs.add(TabInfo(initialHtml = BrowserConfig.INITIAL_HTML))
                        activeTabIndex = 0
                    }
                },
                onNewTab = {
                    val newTab = TabInfo(initialHtml = BrowserConfig.INITIAL_HTML, title = mutableStateOf("Home"))
                    tabs.add(newTab)
                    activeTabIndex = tabs.lastIndex
                }
            )

            val loadingState = activeTabState?.loadingState
            if (loadingState is LoadingState.Loading) {
                LinearProgressIndicator(loadingState.progress, Modifier.fillMaxWidth())
            }

            BrowserBody(
                sidebarVisible = sidebarVisible,
                onSiteClick = { label, host ->
                    val newTab = TabInfo(initialUrl = "https://$host", title = mutableStateOf(label))
                    tabs.add(newTab)
                    activeTabIndex = tabs.lastIndex
                },
                content = {
                    Box(Modifier.fillMaxSize()) {
                        tabs.forEachIndexed { index, tabInfo ->
                            key(tabInfo.id) { // generate key content
                                var state = tabStateMap[tabInfo.id]?.first
                                val isHasCache = state != null
                                var navigator = tabStateMap[tabInfo.id]?.second
                                if (state == null) {
                                    state = if (tabInfo.initialHtml != null) {
                                        rememberWebViewStateWithHTMLData(data = tabInfo.initialHtml)
                                    } else if (tabInfo.initialUrl != null) {
                                        rememberWebViewState(url = tabInfo.initialUrl)
                                    } else {
                                        rememberWebViewStateWithHTMLData(data = BrowserConfig.INITIAL_HTML)
                                    }
                                }
                                navigator = navigator ?: rememberWebViewNavigator()

                                if (!isHasCache) {
                                    tabStateMap[tabInfo.id] = state to navigator
                                }

                                LaunchedEffect(Unit) {
                                    state.webSettings.applyDefault()
                                    setupPlatformWebSettings(state.webSettings)
                                }

                                LaunchedEffect(darkTheme, forceDark) {
                                    // 给页面一点时间渲染，确保 JS 函数可用
                                    delay(100)
                                    if (state.loadingState is LoadingState.Finished || tabInfo.initialHtml != null) {
                                        navigator.evaluateJavaScript("toggleTheme($darkTheme);")
                                    }
                                    toggleForceDarkMode(forceDark, navigator)
                                }

                                WebView(
                                    state = state,
                                    navigator = navigator,
                                    modifier = if (index == activeTabIndex) {
                                        Modifier.fillMaxSize()
                                    } else {
                                        Modifier.size(0.dp)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

data class AllowedSite(val label: String, val host: String)
object BrowserConfig {
    const val INITIAL_HTML: String =
        """<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"><style>*{margin:0;padding:0;box-sizing:border-box;}html,body{height:100vh;width:100vw;font-family:'Arial',sans-serif;overflow:hidden;transition:background-color .3s,color .3s;}.dark{background:#1e1f22;color:white;}.light{background:#ffffff;color:#000}.container{display:flex;justify-content:center;align-items:center;width:100vw;height:100vh;}h1{font-size:6em;}</style></head><body class=\"dark\"><div class=\"container\"><h1>Hee</h1></div><script>function toggleTheme(isDark){const b=document.body;if(isDark){b.classList.remove('light');b.classList.add('dark');}else{b.classList.remove('dark');b.classList.add('light');}}</script></body></html>"""
    val ALLOWED_SITES = listOf(
        AllowedSite("Gemini", "gemini.google.com"),
        AllowedSite("Google", "www.google.com"),
        AllowedSite("Kimi", "www.kimi.com"),
        AllowedSite("tianhu", "www.aitianhu.com")
    )
    val ALLOWED_PATTERNS = listOf(
        ".*://.*aitianhu.*",
        ".*://.*google\\.com.*",
        ".*://.*kimi\\.com.*",
        ".*://.*deepseek\\.com.*",

        ".*://.*alicdn\\.com.*",
        ".*://.*localhost.*",
        ".*://.*127\\.0\\.0\\.1.*",
        "^(file|data)://.*"
    ).map { Regex(it, RegexOption.IGNORE_CASE) }
}

private fun isUrlAllowed(url: String): Boolean {
    if (BrowserConfig.ALLOWED_SITES.any { url.contains(it.host, ignoreCase = true) }) return true
    return BrowserConfig.ALLOWED_PATTERNS.any { it.matches(url) }
}

private fun createRequestInterceptor(): RequestInterceptor = object : RequestInterceptor {
    override fun onInterceptUrlRequest(request: WebRequest, navigator: WebViewNavigator): WebRequestInterceptResult {
        if (!request.isForMainFrame) return WebRequestInterceptResult.Allow
        return if (isUrlAllowed(request.url.lowercase())) WebRequestInterceptResult.Allow else WebRequestInterceptResult.Reject
    }
}

fun toggleForceDarkMode(enable: Boolean, navigator: WebViewNavigator) {
    val script = if (enable) buildString {
        append("(function(){let style=document.getElementById('force-dark-style');if(style)style.remove();style=document.createElement('style');style.id='force-dark-style';style.textContent=`${CSS_FORCE_DARK}`;document.head.appendChild(style);})();")
    } else {
        "(function(){const style=document.getElementById('force-dark-style');if(style)style.remove();})();"
    }
    navigator.evaluateJavaScript(script)
}

private const val CSS_FORCE_DARK =
    """/* 强制黑暗模式样式 */*,*::before,*::after{background-color:#121212!important;color:#e0e0e0!important;border-color:#333!important;outline-color:#333!important;}a,a:visited{color:#64b5f6!important;}a:hover,a:active{color:#90caf9!important;}input,textarea,select,button{background-color:#1e1e1e!important;color:#e0e0e0!important;border:1px solid #333!important;}button,input[type='button'],input[type='submit'],input[type='reset']{background-color:#333!important;color:#e0e0e0!important;}button:hover{background-color:#424242!important;}pre,code{background-color:#0d1117!important;color:#f0f6fc!important;border:1px solid #30363d!important;}table,th,td{background-color:#1e1e1e!important;color:#e0e0e0!important;border-color:#333!important;}th{background-color:#333!important;}img,video,iframe,embed,object{filter:brightness(.8) contrast(1.2)!important;}svg{filter:invert(1) hue-rotate(180deg)!important;}::selection{background-color:#3700b3!important;color:#fff!important;}::-webkit-scrollbar{background-color:#1e1e1e!important;}::-webkit-scrollbar-thumb{background-color:#333!important;}::-webkit-scrollbar-thumb:hover{background-color:#424242!important;}[style*='background-color: white'],[style*='background-color: #fff'],[style*='background-color: #ffffff']{background-color:#121212!important;}[style*='color: black'],[style*='color: #000'],[style*='color: #000000']{color:#e0e0e0!important;}"""

fun WebSettings.applyDefault() {
    logSeverity = KLogSeverity.Debug
    customUserAgentString =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    isJavaScriptEnabled = true
    supportZoom = true
    allowFileAccessFromFileURLs = true
    allowUniversalAccessFromFileURLs = true
    androidWebSettings.apply {
        useWideViewPort = true
    }
}

expect fun setupPlatformWebSettings(webSettings: WebSettings)

expect fun randomUUID(): String
