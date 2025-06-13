package com.hee.sample.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hee.sample.BrowserConfig

@Composable
fun SiteSidebar(onSiteClick: (label: String, site: String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .padding(4.dp)
            .background(Color(0x759FA8))
    ) {
        BrowserConfig.ALLOWED_SITES.forEach { site ->
            Button(
                onClick = { onSiteClick(site.label, site.host) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(1.dp)
            ) {
                Text(site.label, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}
