package com.hee.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun alertPop(
    openFlag: Boolean,
    closeFlagFunc: () -> Unit,
    title: String = "title",
    message: String = "message"
) {
    val openDialog = remember { mutableStateOf(false) }
    openDialog.value = openFlag
    val scope = rememberCoroutineScope()

    var cancelJob: Job? = null
    if (openDialog.value) {
        cancelJob = scope.launch {
            delay(2000L)
            closeFlagFunc()
        }
    }

    if (openDialog.value) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Transparent),
            contentAlignment = Alignment.TopCenter
        ) {
            AlertDialog(
                onDismissRequest = {
                    closeFlagFunc()
                    cancelJob?.cancel()
                },
                confirmButton = {
                    Button(
                        onClick = {
                            closeFlagFunc()
                            cancelJob?.cancel()
                        },
                        modifier = Modifier.fillMaxWidth(0.15f).fillMaxHeight(0.1f).background(Color.Transparent)
                    ) {
                        Text("ok", fontSize = 30.sp, modifier = Modifier.align(Alignment.CenterVertically))
                    }
                },
                title = { Text(title) },
                text = { Text(message) },
            )
        }
    }
}
