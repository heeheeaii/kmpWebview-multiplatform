package com.hee.sample.data

expect class CommonUri {
    val host: String?

    companion object {
        fun parse(uriString: String): CommonUri
    }

}
