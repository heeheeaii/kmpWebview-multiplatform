package com.hee.sample

import ContentPop
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hee.sample.config.BrowserConfig
import com.hee.sample.config.applyDefault
import com.hee.sample.data.TabInfo
import com.hee.sample.ui.BrowserTopBar
import com.hee.sample.ui.SiteSidebar
import com.hee.sample.ui.TabBar
import com.hee.sample.ui.toggleForceDarkMode
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.setting.WebSettings
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.NativeWebView
import com.multiplatform.webview.web.PlatformWebViewParams
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import kotlinx.coroutines.delay

@Composable
fun interceptRequestSample() {
    var forceDark_RS by rememberSaveable { mutableStateOf(true) }
    var sidebarVisible_RS by rememberSaveable { mutableStateOf(true) }

    val tabs_RS = remember {
        mutableStateListOf<TabInfo>()
    }

    var activeTabIndex_RS by rememberSaveable { mutableIntStateOf(0) }

    val tabStateMap_RS = remember { mutableStateMapOf<String, Pair<WebViewState, WebViewNavigator>>() }

    DisposableEffect(Unit) {
        onDispose {
            // Cleanup logic if needed in the future
        }
    }

    LaunchedEffect(Unit) {
        if (tabs_RS.isEmpty()) {
            tabs_RS.add(TabInfo(initialHtml = BrowserConfig.INITIAL_HTML))
        }
    }

    val activeTabInfo = tabs_RS.getOrNull(activeTabIndex_RS)
    val activeTabStateAndNav = activeTabInfo?.id?.let { tabStateMap_RS[it] }
    val activeTabState = activeTabStateAndNav?.first
    var activeNavigator_RS by remember { mutableStateOf(activeTabStateAndNav?.second) }

    val colors = if (forceDark_RS) darkColors() else lightColors()

    var showPop_RS by remember { mutableStateOf(false) } // pop box
    MaterialTheme(colors = colors) {
        Box(Modifier.fillMaxSize()) {
            Column {
                BrowserTopBar(
                    navigator = activeNavigator_RS,
                    forceDark = forceDark_RS,
                    sidebarVisible = sidebarVisible_RS,
                    onToggleForceDark = { forceDark_RS = !forceDark_RS },
                    onToggleSidebar = { sidebarVisible_RS = !sidebarVisible_RS }
                )

                TabBar(
                    tabs = tabs_RS,
                    activeTabIndex = activeTabIndex_RS,
                    onTabSelected = { index -> activeTabIndex_RS = index },
                    onTabClosed = { tabInfo ->
                        tabStateMap_RS.remove(tabInfo.id)
                        tabs_RS.remove(tabInfo)
                        if (activeTabIndex_RS >= tabs_RS.size && tabs_RS.isNotEmpty()) {
                            activeTabIndex_RS = tabs_RS.size - 1
                        } else if (tabs_RS.isEmpty()) {
                            activeTabIndex_RS = 0
                            tabs_RS.add(TabInfo(initialHtml = BrowserConfig.INITIAL_HTML))
                        }
                    },
                    onNewTab = {
                        val newTab = TabInfo(initialHtml = BrowserConfig.INITIAL_HTML, title = mutableStateOf("Home"))
                        tabs_RS.add(newTab)
                        activeTabIndex_RS = tabs_RS.lastIndex
                    }
                )

                val loadingState = activeTabState?.loadingState
                if (loadingState is LoadingState.Loading) {
                    LinearProgressIndicator(loadingState.progress, Modifier.fillMaxWidth())
                }
                Row(Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = sidebarVisible_RS,
                        enter = slideInHorizontally { -it },
                        exit = slideOutHorizontally { -it }
                    ) {
                        SiteSidebar(onSiteClick = { label, host ->
                            val newTab = TabInfo(initialUrl = "https://$host", title = mutableStateOf(label))
                            tabs_RS.add(newTab)
                            activeTabIndex_RS = tabs_RS.lastIndex
                        })
                    }
                    Box(Modifier.weight(1f)) {
                        Box(Modifier.fillMaxSize()) {
                            val boxScope = rememberCoroutineScope()
                            tabs_RS.forEachIndexed { index, tabInfo ->
                                key(tabInfo.id) {
                                    var state = tabStateMap_RS[tabInfo.id]?.first // cache page state
                                    val isHasCache = state != null
                                    if (state == null) {
                                        state = if (tabInfo.initialHtml != null) {
                                            rememberWebViewStateWithHTMLData(data = tabInfo.initialHtml)
                                        } else if (tabInfo.initialUrl != null) {
                                            rememberWebViewState(url = tabInfo.initialUrl)
                                        } else {
                                            rememberWebViewStateWithHTMLData(data = BrowserConfig.INITIAL_HTML)
                                        }
                                        LaunchedEffect(Unit) {
                                            state.webSettings.applyDefault()
                                            setupPlatformWebSettings(state.nativeWebView, state.webSettings)
                                        }
                                    }
                                    var navigator = tabStateMap_RS[tabInfo.id]?.second
                                    if (navigator == null) {
                                        navigator = rememberWebViewNavigator(
                                            coroutineScope = boxScope,
                                            requestInterceptor = remember {
                                                createRequestInterceptor()
                                            }
                                        )
                                    }

                                    if (!isHasCache) {
                                        tabStateMap_RS[tabInfo.id] = state to navigator
                                    }

                                    if (index == activeTabIndex_RS) {
                                        LaunchedEffect(navigator) {
                                            activeNavigator_RS = navigator
                                        }
                                        LaunchedEffect(forceDark_RS, state.loadingState) {
                                            if (state.loadingState is LoadingState.Finished || tabInfo.initialHtml != null) {
                                                // 给页面一点时间渲染，确保 JS 函数可用
                                                delay(100)
                                                navigator.evaluateJavaScript("toggleTheme($forceDark_RS);")
                                                toggleForceDarkMode(forceDark_RS, navigator)
                                            }
                                        }
                                    }

                                    val webViewModifier = if ((index == activeTabIndex_RS) && !showPop_RS) {
                                        Modifier.fillMaxSize()
                                    } else {
                                        Modifier.size(0.dp)
                                    }

                                    WebView(
                                        state = state,
                                        navigator = navigator,
                                        modifier = webViewModifier,
                                        platformWebViewParams = getPlatformWebViewParams(),
                                    )
                                }
                            }
                        }
                    }
                }
                if (showPop_RS) {
                    ContentPop(
                        modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.8f).background(Color(0x51A818)),
                        onDismissRequest = { showPop_RS = !showPop_RS },
                        title = "温馨提示",
                        text = "此为通用内容弹窗，可用于展示任意内容！"
                    )
                }
            }
        }
    }
}

private fun isUrlAllowed(url: String): Boolean {
    if (BrowserConfig.ALLOWED_SITES.any { url.contains(it.host, ignoreCase = true) }) return true
    return BrowserConfig.ALLOWED_PATTERNS.any { it.matches(url) }
}

private fun createRequestInterceptor(): RequestInterceptor = object : RequestInterceptor {
    // interrupt function
    override fun onInterceptUrlRequest(request: WebRequest, navigator: WebViewNavigator): WebRequestInterceptResult {
        if (!request.isForMainFrame) return WebRequestInterceptResult.Allow
        return if (isUrlAllowed(request.url.lowercase())) WebRequestInterceptResult.Allow else WebRequestInterceptResult.Reject
    }
}

expect fun setupPlatformWebSettings(nativeWebView: NativeWebView, webSettings: WebSettings)

expect fun randomUUID(): String

@Composable
expect fun getPlatformWebViewParams(): PlatformWebViewParams?
