package com.hee.sample

// commonTest
import com.hee.sample.encrypt.EncryptionService
import kotlin.test.Test
import kotlin.test.assertEquals

class CommonTests {
    @Test
    fun testExample() {
        val deviceId = "12345"
        val decryptionPassword = "mySecretPassword"
        val obfuscationChars = "randomChars"

        val encryptedString = EncryptionService.generateEncryptedString(
            deviceId = deviceId,
            decryptionPassword = decryptionPassword,
            obfuscationChars = obfuscationChars
        )
        println("Encrypted String: $encryptedString")

        val decryptedData = EncryptionService.decryptEncryptedString(encryptedString)
        println("Decrypted Data: $decryptedData")    }
}
