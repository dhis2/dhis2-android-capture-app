package org.dhis2.form.ui.plugin

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.google.gson.Gson
import org.dhis2.form.ui.plugin.path.AssetsSubdirPathHandler
import org.dhis2.form.ui.plugin.path.Dhis2SdkApiPathHandler

private const val HOST = "appassets.androidplatform.net"
private const val HOST_ASSETS_DIR = "dhis2-plugin-host"
private const val BUNDLED_PLUGINS_DIR = "plugins"
private const val HOST_PATH = "/host/"
private const val PLUGINS_PATH = "/plugins/"
private const val API_PATH = "/api/"
private const val BRIDGE_URL = "https://$HOST${HOST_PATH}bridge.html"

@Composable
fun PluginWebViewHost(
    pluginId: String,
    pluginVersion: String,
    props: PluginProps,
    onSetFieldValue: (SetFieldValueParams) -> Unit,
    onSetContextFieldValue: (SetContextFieldValueParams) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gson = remember { Gson() }

    val pluginUrl = "https://$HOST${PLUGINS_PATH}$pluginId-$pluginVersion/plugin.html"

    // Wrap in a Box so Compose's size constraints are applied to the container, and the
    // AndroidView just fills it. The WebView also gets explicit MATCH_PARENT layout params
    // because Compose doesn't always propagate size to an unparented Android View — without
    // this, WebView.innerHeight reports 0 even when the view visually occupies full space.
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val assetLoader = WebViewAssetLoader.Builder()
                    .setDomain(HOST)
                    .addPathHandler(HOST_PATH, AssetsSubdirPathHandler(ctx, HOST_ASSETS_DIR))
                    .addPathHandler(PLUGINS_PATH, AssetsSubdirPathHandler(ctx, BUNDLED_PLUGINS_DIR))
                    .addPathHandler(API_PATH, Dhis2SdkApiPathHandler())
                    .build()

                @SuppressLint("SetJavaScriptEnabled")
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    WebView.setWebContentsDebuggingEnabled(true)

                    addJavascriptInterface(
                        PluginBridge(onSetFieldValue, onSetContextFieldValue),
                        PluginBridge.JS_NAME,
                    )

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest,
                        ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

                        override fun onPageFinished(view: WebView, url: String?) {
                            if (url == BRIDGE_URL) {
                                val config = mapOf(
                                    "pluginUrl" to pluginUrl,
                                    "props" to props,
                                )
                                val configJson = gson.toJson(config)
                                    .replace("\\", "\\\\")
                                    .replace("'", "\\'")
                                    .replace("\n", "")
                                    .replace("\r", "")
                                view.evaluateJavascript(
                                    "window.configurePlugin(JSON.parse('$configJson'));",
                                    null,
                                )
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                            Log.d(
                                TAG,
                                "${message.messageLevel()} ${message.message()} @ " +
                                    "${message.sourceId()}:${message.lineNumber()}",
                            )
                            return true
                        }
                    }

                    loadUrl(BRIDGE_URL)
                }
            },
            update = { webView ->
                val propsJson = gson.toJson(props)
                webView.evaluateJavascript(
                    "if (window.updateProps) window.updateProps(${gson.toJson(propsJson)});",
                    null,
                )
            },
        )
    }
}

private const val TAG = "PluginWebViewHost"
