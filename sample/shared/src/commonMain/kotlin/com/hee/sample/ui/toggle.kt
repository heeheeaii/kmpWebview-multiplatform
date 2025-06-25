package com.hee.sample.ui

import com.multiplatform.webview.web.WebViewNavigator

private const val CSS_FORCE_DARK =
    """/* 强制黑暗模式样式 */*,*::before,*::after{background-color:#121212!important;color:#e0e0e0!important;border-color:#333!important;outline-color:#333!important;}a,a:visited{color:#64b5f6!important;}a:hover,a:active{color:#90caf9!important;}input,textarea,select,button{background-color:#1e1e1e!important;color:#e0e0e0!important;border:1px solid #333!important;}button,input[type='button'],input[type='submit'],input[type='reset']{background-color:#333!important;color:#e0e0e0!important;}button:hover{background-color:#424242!important;}pre,code{background-color:#0d1117!important;color:#f0f6fc!important;border:1px solid #30363d!important;}table,th,td{background-color:#1e1e1e!important;color:#e0e0e0!important;border-color:#333!important;}th{background-color:#333!important;}img,video,iframe,embed,object{filter:brightness(.8) contrast(1.2)!important;}svg{filter:invert(1) hue-rotate(180deg)!important;}::selection{background-color:#3700b3!important;color:#fff!important;}::-webkit-scrollbar{background-color:#1e1e1e!important;}::-webkit-scrollbar-thumb{background-color:#333!important;}::-webkit-scrollbar-thumb:hover{background-color:#424242!important;}[style*='background-color: white'],[style*='background-color: #fff'],[style*='background-color: #ffffff']{background-color:#121212!important;}[style*='color: black'],[style*='color: #000'],[style*='color: #000000']{color:#e0e0e0!important;}"""

fun toggleForceDarkMode(enable: Boolean, navigator: WebViewNavigator) {
    val script = if (enable) buildString {
        append("(function(){let style=document.getElementById('force-dark-style');if(style)style.remove();style=document.createElement('style');style.id='force-dark-style';style.textContent=`${CSS_FORCE_DARK}`;document.head.appendChild(style);})();")
    } else {
        "(function(){const style=document.getElementById('force-dark-style');if(style)style.remove();})();"
    }
    navigator.evaluateJavaScript(script)
}
