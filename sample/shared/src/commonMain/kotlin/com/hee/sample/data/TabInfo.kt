package com.hee.sample.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.hee.sample.randomUUID

data class TabInfo(
    val id: String = randomUUID(),
    val initialUrl: String? = null,
    val initialHtml: String? = null,
    var title: MutableState<String> = mutableStateOf("Home")
)
