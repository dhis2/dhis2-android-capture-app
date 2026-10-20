package org.dhis2.mobile.plugin.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.dhis2.mobile.plugin.api.PluginApiRouter
import org.dhis2.mobile.plugin.di.WEB_PLUGINS_DIR
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.koin.compose.koinInject
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File

private const val HOST = "appassets.androidplatform.net"
private const val ORIGIN = "https://$HOST"
private const val BRIDGE_URL = "$ORIGIN/host/dhis2-plugin-host/bridge.html"
private const val SHIM_ASSET = "dhis2-plugin-host/api-shim.js"
private const val BRIDGE_OBJECT = "dhis2Native"
private const val SET_PLUGIN_HEIGHT = "setPluginHeight"

/**
 * Renders an unmodified DHIS2 web plugin from its bundle on the device.
 *
 * Everything the WebView loads comes from the app: the bridge page from the APK's assets, the
 * plugin from its extracted bundle, and every `/api/` call from [PluginApiRouter] over the
 * `dhis2Native` message bridge. Requests to any other host are refused, and so is navigating
 * away, so a plugin renders and works the same with no connection at all.
 *
 * @param props Passed to the plugin as its props, and pushed again whenever they change.
 * @param onCallback Called when the plugin invokes a function prop, such as `setFieldValue`.
 * @param initialHeight Height until the plugin reports its own through `setPluginHeight`.
 */
@Composable
fun WebPlugin(
    plugin: RegisteredPlugin,
    modifier: Modifier = Modifier,
    props: JsonObject = JsonObject(emptyMap()),
    onCallback: (PluginCallback) -> Unit = {},
    initialHeight: Dp = 240.dp,
    maxHeight: Dp = 800.dp,
) {
    if (!WebPluginSupport.isAvailable()) {
        InfoBar(
            modifier = modifier,
            text = "Plugin '${plugin.key}' needs a newer Android System WebView",
            icon = {},
            textColor = SurfaceColor.Warning,
            backgroundColor = SurfaceColor.WarningContainer,
        )
        return
    }

    val router = koinInject<PluginApiRouter>()
    val scope = rememberCoroutineScope()
    val currentOnCallback by rememberUpdatedState(onCallback)
    var height by remember(plugin) { mutableStateOf(initialHeight) }

    val session =
        remember(plugin) {
            PluginBridgeSession(
                pluginUrl = "$ORIGIN/plugins/${plugin.bundleDir.name}/${plugin.entry}",
                router = router,
                scope = scope,
                onCallback = { callback ->
                    if (callback.name == SET_PLUGIN_HEIGHT) {
                        callback.args.jsonPrimitive.doubleOrNull?.let {
                            height = it.dp.coerceIn(1.dp, maxHeight)
                        }
                    } else {
                        currentOnCallback(callback)
                    }
                },
            )
        }

    AndroidView(
        modifier = modifier.fillMaxWidth().height(height),
        factory = { context -> pluginWebView(context, session) },
        update = { session.updateProps(props) },
        onRelease = { it.destroy() },
    )
}

/** Whether this device's WebView can host plugins: both bridge features are required. */
object WebPluginSupport {
    fun isAvailable(): Boolean =
        WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER) &&
            WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)
}

@SuppressLint("SetJavaScriptEnabled", "RequiresFeature")
private fun pluginWebView(
    context: Context,
    session: PluginBridgeSession,
): WebView {
    val assetLoader =
        WebViewAssetLoader
            .Builder()
            .setDomain(HOST)
            .addPathHandler("/host/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler(
                "/plugins/",
                WebViewAssetLoader.InternalStoragePathHandler(context, File(context.filesDir, WEB_PLUGINS_DIR)),
            ).build()
    val shim =
        context.assets
            .open(SHIM_ASSET)
            .bufferedReader()
            .use { it.readText() }

    return WebView(context).apply {
        // Compose sizes the AndroidView, but the WebView's viewport stays 0 high without these.
        layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.setSupportMultipleWindows(false)
        WebView.setWebContentsDebuggingEnabled(context.isDebuggable())

        // Only frames served from the asset-loader origin get the bridge or the shim.
        WebViewCompat.addWebMessageListener(this, BRIDGE_OBJECT, setOf(ORIGIN)) { _, message, _, isMainFrame, replyProxy ->
            session.onMessage(message.data, isMainFrame, replyProxy)
        }
        WebViewCompat.addDocumentStartJavaScript(this, shim, setOf(ORIGIN))

        webViewClient =
            object : WebViewClientCompat() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest,
                ): WebResourceResponse =
                    if (request.url.host == HOST) {
                        assetLoader.shouldInterceptRequest(request.url) ?: emptyResponse(404, "Not Found")
                    } else {
                        Timber.w("Plugin request to %s refused: plugins only load local content", request.url)
                        emptyResponse(403, "Forbidden")
                    }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest,
                ): Boolean = request.url.host != HOST
            }

        loadUrl(BRIDGE_URL)
    }
}

private fun emptyResponse(
    status: Int,
    reason: String,
) = WebResourceResponse("text/plain", "utf-8", status, reason, emptyMap(), ByteArrayInputStream(ByteArray(0)))

private fun Context.isDebuggable(): Boolean = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
