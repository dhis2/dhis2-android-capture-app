package org.dhis2.mobile.extension.web.host

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File

private const val HOST = "appassets.androidplatform.net"

/** The only origin the WebView loads from, and the only one that gets the bridge. */
const val ORIGIN = "https://$HOST"

/** Where the host pages shipped in the APK's assets are served from. */
const val HOST_PAGES_URL = "$ORIGIN/host/extension-web"

/** Where the files of [hostWebView]'s `bundlesDir` are served from. */
const val BUNDLES_URL = "$ORIGIN/bundles"

private const val SHIM_ASSET = "extension-web/api-shim.js"
private const val BRIDGE_OBJECT = "dhis2Native"

/** Whether this device's WebView can host web extensions: both bridge features are required. */
object WebViewSupport {
    fun isAvailable(): Boolean =
        WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER) &&
            WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)
}

/**
 * A WebView that loads only local content, starting at [startUrl]: the host pages from the APK's
 * assets under `/host/` and, when [bundlesDir] is given, the files below it under `/bundles/`.
 * Requests to any other host are refused, and so is navigating away, so the content renders and
 * works the same with no connection at all.
 *
 * Messages on the `dhis2Native` bridge go to [onMessage], with whether the main frame sent them;
 * `api-shim.js` routes every `/api/` call through it.
 *
 * PoC: everything runs in one origin. The real development isolates the content (separate
 * origins or a sandboxed iframe) and adds a CSP.
 *
 * @param onRendererGone The WebView's render process crashed or was killed; the view is unusable.
 */
@SuppressLint("SetJavaScriptEnabled", "RequiresFeature")
fun hostWebView(
    context: Context,
    startUrl: String,
    onMessage: (data: String?, isMainFrame: Boolean, replyProxy: JavaScriptReplyProxy) -> Unit,
    zoomable: Boolean = false,
    bundlesDir: File? = null,
    onRendererGone: () -> Unit = {},
): WebView {
    val assetLoader =
        WebViewAssetLoader
            .Builder()
            .setDomain(HOST)
            .addPathHandler("/host/", WebViewAssetLoader.AssetsPathHandler(context))
            .apply {
                bundlesDir?.let {
                    addPathHandler("/bundles/", WebViewAssetLoader.InternalStoragePathHandler(context, it))
                }
            }.build()
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
        settings.setSupportZoom(zoomable)
        settings.builtInZoomControls = zoomable
        settings.displayZoomControls = false
        WebView.setWebContentsDebuggingEnabled(context.isDebuggable())

        // Only frames served from the asset-loader origin get the bridge or the shim.
        WebViewCompat.addWebMessageListener(this, BRIDGE_OBJECT, setOf(ORIGIN)) { _, message, _, isMainFrame, replyProxy ->
            onMessage(message.data, isMainFrame, replyProxy)
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
                        Timber.w("Request to %s refused: web extensions only load local content", request.url)
                        emptyResponse(403, "Forbidden")
                    }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest,
                ): Boolean = request.url.host != HOST

                // Returning true keeps the app alive when the renderer dies.
                override fun onRenderProcessGone(
                    view: WebView,
                    detail: RenderProcessGoneDetail,
                ): Boolean {
                    Timber.w("WebView renderer gone (crashed=%s)", detail.didCrash())
                    onRendererGone()
                    return true
                }
            }

        loadUrl(startUrl)
    }
}

private fun emptyResponse(
    status: Int,
    reason: String,
) = WebResourceResponse("text/plain", "utf-8", status, reason, emptyMap(), ByteArrayInputStream(ByteArray(0)))

private fun Context.isDebuggable(): Boolean = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
