package org.dhis2.form.ui.plugin.path

import android.util.Log
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import org.dhis2.form.ui.plugin.api.Dhis2SdkApiDispatcher
import java.io.ByteArrayInputStream

class Dhis2SdkApiPathHandler(
    private val dispatcher: Dhis2SdkApiDispatcher = Dhis2SdkApiDispatcher(),
) : WebViewAssetLoader.PathHandler {

    override fun handle(path: String): WebResourceResponse {
        val body = try {
            dispatcher.get(path)
        } catch (t: Throwable) {
            Log.e(TAG, "dispatcher failed for /api/$path", t)
            return jsonResponse("{}", status = 500, reason = "SDK error")
        }
        return jsonResponse(body ?: "{}")
    }

    private fun jsonResponse(
        body: String,
        status: Int = 200,
        reason: String = "OK",
    ): WebResourceResponse = WebResourceResponse(
        "application/json",
        "utf-8",
        status,
        reason,
        mapOf("Access-Control-Allow-Origin" to "*"),
        ByteArrayInputStream(body.toByteArray(Charsets.UTF_8)),
    )

    companion object {
        private const val TAG = "Dhis2SdkApiPathHandler"
    }
}
