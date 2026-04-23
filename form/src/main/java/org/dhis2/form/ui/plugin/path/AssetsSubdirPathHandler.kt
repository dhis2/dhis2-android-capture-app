package org.dhis2.form.ui.plugin.path

import android.content.Context
import android.util.Log
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import java.io.IOException

class AssetsSubdirPathHandler(
    context: Context,
    private val subdir: String,
) : WebViewAssetLoader.PathHandler {
    private val assets = context.applicationContext.assets

    override fun handle(path: String): WebResourceResponse? {
        val relative = path.trimStart('/')
        val assetPath = if (relative.isEmpty()) subdir else "$subdir/$relative"
        return try {
            val stream = assets.open(assetPath)
            WebResourceResponse(guessMimeType(relative), null, stream)
        } catch (e: IOException) {
            Log.w(TAG, "Asset not found: $assetPath")
            null
        }
    }

    private fun guessMimeType(path: String): String =
        when (path.substringAfterLast('.', "").lowercase()) {
            "html", "htm" -> "text/html"
            "js", "mjs" -> "application/javascript"
            "css" -> "text/css"
            "json", "webapp", "map" -> "application/json"
            "webmanifest" -> "application/manifest+json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "svg" -> "image/svg+xml"
            "ico" -> "image/x-icon"
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            "ttf" -> "font/ttf"
            "xml" -> "application/xml"
            else -> "application/octet-stream"
        }

    companion object {
        private const val TAG = "AssetsSubdirPathHandler"
    }
}
