package com.hee.sample.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BrowserBody(
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
