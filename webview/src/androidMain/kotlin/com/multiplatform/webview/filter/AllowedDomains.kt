package com.multiplatform.webview.filter

object AllowedDomains {
    val whitelist = setOf(
        "example.com",
        "another-allowed-domain.org",
        "sub.example.com",
    )

    fun isDomainAllowed(url: String): Boolean {
        return try {
            val host = android.net.Uri.parse(url).host ?: return false

            if (whitelist.contains(host)) {
                return true
            }

            for (allowedDomain in whitelist) {
                if (host.endsWith(".$allowedDomain") || host == allowedDomain) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
