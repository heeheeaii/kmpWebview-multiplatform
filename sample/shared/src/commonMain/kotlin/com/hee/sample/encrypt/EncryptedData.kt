package com.hee.sample.encrypt

data class EncryptedData(
    val deviceId: String,
    val startTime: Long,
    val validTime: Long,
    val decryptionPassword: String,
    val obfuscationChars: String
)
