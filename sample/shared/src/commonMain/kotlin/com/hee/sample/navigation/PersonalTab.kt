package com.hee.sample.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState


@Composable
fun Personal() {
    val state = rememberWebViewState("https://www.jetbrains.com/lp/compose-multiplatform/")
    WebView(state = state, modifier = Modifier.fillMaxSize().padding(bottom = 45.dp))
}
