// In src/androidMain/kotlin/your/package/previews/TabBarPreview.kt

import androidx.compose.runtime.Composable
import com.hee.sample.TabInfo
import com.hee.sample.ui.TabBar


@Composable
fun TabBarPreview() {
    // You can provide dummy data here for the preview
    val sampleTabs = listOf(
        TabInfo("Home"),
        TabInfo("Profile"),
        TabInfo("Settings")
    )

    TabBar(
        tabs = sampleTabs,
        activeTabIndex = 0,
        onTabSelected = {},
        onTabClosed = {},
        onNewTab = {}
    )
}

// Assuming your TabInfo is something like this in commonMain
// data class TabInfo(val title: String)
