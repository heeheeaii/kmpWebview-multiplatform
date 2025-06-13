package com.hee.sample.config

data class AllowedSite(val label: String, val host: String)

object BrowserConfig {
    const val INITIAL_HTML: String =
        """<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"><style>*{margin:0;padding:0;box-sizing:border-box;}html,body{height:100vh;width:100vw;font-family:'Arial',sans-serif;overflow:hidden;transition:background-color .3s,color .3s;}.dark{background:#1e1f22;color:white;}.light{background:#ffffff;color:#000}.container{display:flex;justify-content:center;align-items:center;width:100vw;height:100vh;}h1{font-size:6em;}</style></head><body class=\"dark\"><div class=\"container\"><h1>Hee</h1></div><script>function toggleTheme(isDark){const b=document.body;if(isDark){b.classList.remove('light');b.classList.add('dark');}else{b.classList.remove('dark');b.classList.add('light');}}</script></body></html>"""
    val ALLOWED_SITES = listOf(
        AllowedSite("Gemini", "gemini.google.com"),
        AllowedSite("Google", "www.google.com"),
        AllowedSite("Kimi", "www.kimi.com"),
        AllowedSite("tianhu", "www.aitianhu.com")
    )
    val ALLOWED_PATTERNS = listOf(
        ".*://.*aitianhu.*",
        ".*://.*google\\.com.*",
        ".*://.*kimi\\.com.*",
        ".*://.*deepseek\\.com.*",

        ".*://.*alicdn\\.com.*",
        ".*://.*localhost.*",
        ".*://.*127\\.0\\.0\\.1.*",
        "^(file|data)://.*"
    ).map { Regex(it, RegexOption.IGNORE_CASE) }
}
