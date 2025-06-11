package com.hee.sample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
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

/* --------------------------------------------------------------------------
 * DATA & STATE HOLDERS
 * --------------------------------------------------------------------------*/

data class TabState(
    val state: WebViewState,
    val navigator: WebViewNavigator
)

data class TabInfo(
    val id: String = randomUUID(),
    val initialUrl: String? = null,
    val initialHtml: String? = null,
    var title: MutableState<String> = mutableStateOf(if (initialUrl != null) "Loading..." else "Home")
)

/* --------------------------------------------------------------------------
 * PUBLIC API
 * --------------------------------------------------------------------------*/

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
                    val newTab = TabInfo(initialUrl = "https://gemini.google.com", title = mutableStateOf("Gemini"))
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
                onSiteClick = { host ->
                    val newTab = TabInfo(initialUrl = "https://$host", title = mutableStateOf(host))
                    tabs.add(newTab)
                    activeTabIndex = tabs.lastIndex
                },
                content = {
                    Box(Modifier.fillMaxSize()) {
                        tabs.forEachIndexed { index, tabInfo ->
                            key(tabInfo.id) {
                                val state = if (tabInfo.initialHtml != null) {
                                    rememberWebViewStateWithHTMLData(data = tabInfo.initialHtml)
                                } else {
                                    rememberWebViewState(url = tabInfo.initialUrl ?: "about:blank")
                                }
                                val navigator = rememberWebViewNavigator()

                                tabStateMap[tabInfo.id] = state to navigator

                                val pageTitle = state.pageTitle
                                LaunchedEffect(pageTitle) {
                                    if (!pageTitle.isNullOrBlank()) {
                                        tabInfo.title.value = pageTitle
                                    }
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

//@Composable
//fun InterceptRequestSample(navController: NavHostController? = null) {
//    var darkTheme by rememberSaveable { mutableStateOf(true) }
//    var forceDark by rememberSaveable { mutableStateOf(false) }
//    var sidebarVisible by rememberSaveable { mutableStateOf(true) }
//
//    val tabs = remember { mutableStateListOf<TabInfo>() }
//    var activeTabIndex by rememberSaveable { mutableIntStateOf(0) }
//    val tabStateMap = remember { mutableStateMapOf<String, Pair<WebViewState, WebViewNavigator>>() }
//    DisposableEffect(Unit) {
//        onDispose {
//            // 在此方案中，Compose 会自动处理大部分清理工作
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        if (tabs.isEmpty()) {
//            tabs.add(TabInfo(initialHtml = BrowserConfig.INITIAL_HTML))
//        }
//    }
//
//    val activeTabInfo = tabs.getOrNull(activeTabIndex)
//    val activeTabStateAndNav = activeTabInfo?.id?.let { tabStateMap[it] }
//    val activeTabState = activeTabStateAndNav?.first
//    val activeNavigator = activeTabStateAndNav?.second
//
//    val colors = if (darkTheme) darkColors() else lightColors()
//
//    MaterialTheme(colors = colors) {
//        Column {
//            BrowserTopBar(
//                navigator = activeNavigator,
//                onBack = {
//                    if (activeNavigator?.canGoBack == true) activeNavigator.navigateBack() else navController?.popBackStack()
//                },
//                darkTheme = darkTheme,
//                forceDark = forceDark,
//                sidebarVisible = sidebarVisible,
//                onToggleDarkTheme = {
//                    darkTheme = !darkTheme
//                },
//                onToggleForceDark = {
//                    forceDark = !forceDark
//                },
//                onToggleSidebar = { sidebarVisible = !sidebarVisible }
//            )
//
//            TabBar(
//                tabs = tabs,
//                activeTabIndex = activeTabIndex,
//                onTabSelected = { index -> activeTabIndex = index },
//                onTabClosed = { tabInfo ->
//                    tabStateMap.remove(tabInfo.id)
//                    tabs.remove(tabInfo)
//                    if (activeTabIndex >= tabs.size && tabs.isNotEmpty()) {
//                        activeTabIndex = tabs.size - 1
//                    } else if (tabs.isEmpty()) {
//                        tabs.add(TabInfo(initialHtml = BrowserConfig.INITIAL_HTML))
//                        activeTabIndex = 0
//                    }
//                },
//                onNewTab = {
//                    val newTab = TabInfo(initialUrl = "https://gemini.google.com", title = mutableStateOf("Gemini"))
//                    tabs.add(newTab)
//                    activeTabIndex = tabs.lastIndex
//                }
//            )
//
//
//            val loadingState = activeTabState?.loadingState
//            if (loadingState is LoadingState.Loading) {
//                LinearProgressIndicator(loadingState.progress, Modifier.fillMaxWidth())
//            }
//
//            BrowserBody(
//                sidebarVisible = sidebarVisible,
//                onSiteClick = { host ->
//                    val newTab = TabInfo(initialUrl = "https://$host", title = mutableStateOf(host))
//                    tabs.add(newTab)
//                    activeTabIndex = tabs.lastIndex
//                },
//                content = {
//                    Box(Modifier.fillMaxSize()) {
//                        tabs.forEachIndexed { index, tabInfo ->
//                            key(tabInfo.id) {
//                                val state = if (tabInfo.initialHtml != null) {
//                                    rememberWebViewStateWithHTMLData(data = tabInfo.initialHtml)
//                                } else {
//                                    rememberWebViewState(url = tabInfo.initialUrl ?: "about:blank")
//                                }
//                                val navigator = rememberWebViewNavigator()
//                                tabStateMap[tabInfo.id] = state to navigator
//
//                                val pageTitle = state.pageTitle
//                                LaunchedEffect(pageTitle) {
//                                    if (!pageTitle.isNullOrBlank()) {
//                                        tabInfo.title.value = pageTitle
//                                    }
//                                }
//
//                                LaunchedEffect(Unit) {
//                                    state.webSettings.applyDefault()
//                                    setupPlatformWebSettings(state.webSettings)
//                                }
//
//                                // 切换主题和强制暗黑模式
//                                LaunchedEffect(darkTheme, forceDark) {
//                                    delay(100)
//                                    if (state.loadingState is LoadingState.Finished) {
//                                        navigator.evaluateJavaScript("toggleTheme($darkTheme);")
//                                    }
//                                    toggleForceDarkMode(forceDark, navigator)
//                                }
//
//                                WebView(
//                                    state = state,
//                                    navigator = navigator,
//                                    modifier = if (index == activeTabIndex) {
//                                        Modifier.fillMaxSize()
//                                    } else {
//                                        Modifier.size(0.dp) // 隐藏并使其不占空间
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
//            )
//        }
//    }
//}


/* --------------------------------------------------------------------------
 * COMPOSABLE BUILDING BLOCKS
 * (BrowserTopBar, TabBar, BrowserBody, SiteSidebar, TabContent... etc)
 * ... (这些部分代码无须修改，保持原样即可) ...
 * --------------------------------------------------------------------------*/
@Composable
private fun BrowserTopBar(
    navigator: WebViewNavigator?,
    darkTheme: Boolean,
    forceDark: Boolean,
    sidebarVisible: Boolean,
    onBack: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onToggleForceDark: () -> Unit,
    onToggleSidebar: () -> Unit
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack, enabled = navigator?.canGoBack ?: false) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { navigator?.navigateForward() }, enabled = navigator?.canGoForward ?: false) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
            }
            IconButton(onClick = onToggleForceDark) {
                Text(if (forceDark) "\uD83D\uDD05" else "🔆", fontSize = 20.sp)
            }
            IconButton(onClick = onToggleDarkTheme) {
                Text(if (darkTheme) "\uD83C\uDF19" else "☀", fontSize = 20.sp)
            }
            IconButton(onClick = onToggleSidebar) {
                Icon(
                    imageVector = if (sidebarVisible) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (sidebarVisible) "Hide sidebar" else "Show sidebar"
                )
            }
        }
    )
}

@Composable
private fun TabBar(
    tabs: List<TabInfo>,
    activeTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onTabClosed: (TabInfo) -> Unit,
    onNewTab: () -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = activeTabIndex,
        edgePadding = 0.dp,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        tabs.forEachIndexed { index, tabInfo ->
            Tab(
                selected = index == activeTabIndex,
                onClick = { onTabSelected(index) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tabInfo.title.value,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(
                            onClick = { onTabClosed(tabInfo) },
                            modifier = Modifier.size(28.dp).padding(start = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close Tab",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )
        }
        // Button to add a new tab
        IconButton(onClick = onNewTab, modifier = Modifier.padding(4.dp)) {
            Icon(Icons.Default.Add, contentDescription = "New Tab")
        }
    }
}


@Composable
private fun BrowserBody(
    sidebarVisible: Boolean,
    onSiteClick: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = sidebarVisible,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it }
        ) {
            SiteSidebar(onSiteClick = onSiteClick)
        }
        Box(Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
private fun SiteSidebar(onSiteClick: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxHeight()
            .width(120.dp)
            .padding(8.dp)
    ) {
        BrowserConfig.ALLOWED_SITES.forEach { site ->
            Button(
                onClick = { onSiteClick(site.host) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(site.label, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}


@Composable
private fun TabContent(tabState: TabState, darkTheme: Boolean, forceDark: Boolean) {
    val state = tabState.state
    val navigator = tabState.navigator

    LaunchedEffect(state.loadingState, darkTheme) {
        if (state.loadingState is LoadingState.Finished) {
            delay(100)
            navigator.evaluateJavaScript("toggleTheme($darkTheme);")
        }
    }

    LaunchedEffect(forceDark) {
        toggleForceDarkMode(forceDark, navigator)
    }

    WebView(
        state = state,
        navigator = navigator,
        modifier = Modifier.fillMaxSize()
    )
}

/* --------------------------------------------------------------------------
 * BUSINESS LOGIC / HELPERS
 * --------------------------------------------------------------------------*/

@Composable
fun rememberTabContentState(tabInfo: TabInfo): TabState {
    val state = if (tabInfo.initialHtml != null) {
        rememberWebViewStateWithHTMLData(data = tabInfo.initialHtml)
    } else {
        rememberWebViewState(url = tabInfo.initialUrl ?: "")
    }

    val navigator = rememberWebViewNavigator(requestInterceptor = remember {
        createRequestInterceptor()
    })

    DisposableEffect(tabInfo.id) {
        state.webSettings.applyDefault()
        setupPlatformWebSettings(state.webSettings)
        onDispose { }
    }

    return remember(tabInfo.id) { TabState(state, navigator) }
}
/* --------------------------------------------------------------------------
 * The rest of the file (BrowserConfig, isUrlAllowed, etc.) remains unchanged.
 * --------------------------------------------------------------------------*/

data class AllowedSite(val label: String, val host: String)
object BrowserConfig {
    const val INITIAL_HTML: String =
        """<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"><style>*{margin:0;padding:0;box-sizing:border-box;}html,body{height:100vh;width:100vw;font-family:'Arial',sans-serif;overflow:hidden;transition:background-color .3s,color .3s;}.dark{background:#1e1f22;color:white;}.light{background:#ffffff;color:#000}.container{display:flex;justify-content:center;align-items:center;width:100vw;height:100vh;}h1{font-size:6em;}</style></head><body class=\"dark\"><div class=\"container\"><h1>Hee</h1></div><script>function toggleTheme(isDark){const b=document.body;if(isDark){b.classList.remove('light');b.classList.add('dark');}else{b.classList.remove('dark');b.classList.add('light');}}</script></body></html>"""
    val ALLOWED_SITES = listOf(
        AllowedSite("Gemini", "gemini.google.com"),
        AllowedSite("Google", "www.google.com"),
        AllowedSite("Kimi", "www.kimi.com"),
        AllowedSite("sojo", "srv.sojo-ai.com"),
    )
    val ALLOWED_PATTERNS = listOf(
        ".*://.*aitianhu.*",
        ".*://.*google\\.com.*",
        ".*://.*kimi\\.com.*",
        ".*://.*deepseek\\.com.*",

        ".*://.*xchat-ai.*", // sojo ai
        ".*://.*sojo-ai\\.com.*",

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
    customUserAgentString = null
    isJavaScriptEnabled = true
    supportZoom = false
    allowFileAccessFromFileURLs = true
    allowUniversalAccessFromFileURLs = true
}

expect fun setupPlatformWebSettings(webSettings: WebSettings)

expect fun randomUUID(): String
