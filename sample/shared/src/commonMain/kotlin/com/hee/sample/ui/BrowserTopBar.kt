package com.hee.sample.ui

import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.multiplatform.webview.web.WebViewNavigator


@Composable
fun BrowserTopBar(
    navigator: WebViewNavigator?,
    forceDark: Boolean,
    sidebarVisible: Boolean,
    onToggleForceDark: () -> Unit,
    onToggleSidebar: () -> Unit
) {
    TopAppBar(
        title = {},
        modifier = Modifier.height(30.dp),
        actions = {
            IconButton(onClick = {
                navigator?.navigateBack()
            }, enabled = navigator?.canGoBack ?: false) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            IconButton(onClick = { navigator?.navigateForward() }, enabled = navigator?.canGoForward ?: false) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
            }
            IconButton(onClick = onToggleForceDark) {
                Text(if (forceDark) "\uD83D\uDD05" else "🔆", fontSize = 20.sp)
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
//
