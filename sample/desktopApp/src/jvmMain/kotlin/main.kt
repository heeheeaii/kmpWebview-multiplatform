import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hee.sample.MainWebView
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cef.CefApp
import org.cef.handler.CefAppHandlerAdapter
import java.io.File

fun main() =
    application {
        val cefHandler = object : CefAppHandlerAdapter(emptyArray()) {}
        cefHandler.updateArgs(
            arrayOf(
                "--disable-features=BlockThirdPartyCookies,RestrictThirdPartyCookiePartitions",
                "--enable-features=LegacyCookieAccess"
            )
        )
        CefApp.addAppHandler(cefHandler) // enable cookie default, in new chrome will be block default

        Window(onCloseRequest = ::exitApplication) {
            var restartRequired by remember { mutableStateOf(false) }
            var downloading by remember { mutableStateOf(0F) }
            var initialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    KCEF.init(builder = {
                        installDir(File("kcef-bundle"))
                        progress {
                            onDownloading {
                                downloading = it
                            }
                            onInitialized {
                                initialized = true
                            }
                        }
                        download {
                            github {
                                release("jbr-release-17.0.12b1207.37")
                            }
                        }

                        settings {
                            // kcef cache path
                            cachePath = File("cache").absolutePath
                        }
                    }, onError = {
                        it?.printStackTrace()
                    }, onRestartRequired = {
                        restartRequired = true
                    })
                }
            }

            if (restartRequired) {
                Text(text = "Restart required.")
            } else {
                if (initialized) {
                    MainWebView()
                } else {
                    Text(text = "Downloading $downloading%")
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    KCEF.disposeBlocking()
                }
            }
        }
    }
