package com.hee.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hee.sample.TabInfo

@Composable
fun TabBar(
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
                    Box(
                        modifier = Modifier.padding(horizontal = 4.dp).size(30.dp)
                    ) {

                        Text(

                            modifier = Modifier
                                .align(Alignment.Center)

                                .padding(end = 18.dp),
                            text = tabInfo.title.value,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )


                        IconButton(
                            onClick = { onTabClosed(tabInfo) },

                            modifier = Modifier
                                .align(Alignment.TopEnd)


                                .size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Tab",

                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            )
        }

        IconButton(onClick = onNewTab, modifier = Modifier.padding(4.dp)) {
            Icon(Icons.Default.Add, contentDescription = "New Tab")
        }
    }
}
