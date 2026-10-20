package org.dhis2.mobile.plugin.web

import androidx.webkit.JavaScriptReplyProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.dhis2.mobile.plugin.api.ApiRequest
import org.dhis2.mobile.plugin.api.PluginApiRouter
import timber.log.Timber

/** A plugin calling one of the functions the host passed it as a prop, e.g. `setFieldValue`. */
data class PluginCallback(
    val name: String,
    val args: JsonElement,
)

/**
 * The app's end of the `dhis2Native` message bridge for one rendered plugin.
 *
 * Three messages arrive from the WebView:
 * - `ready`, from the bridge page: answered with the plugin URL and the current props.
 * - `api`, from any frame: a captured `/api/` call, answered on the same frame with the router's
 *   response under the same `id`, so concurrent calls resolve independently.
 * - `callback`, from the bridge page: a prop function the plugin called, handed to the host.
 *
 * Messages are accepted only from the asset-loader origin, which the listener is registered with.
 */
internal class PluginBridgeSession(
    private val pluginUrl: String,
    private val router: PluginApiRouter,
    private val scope: CoroutineScope,
    private val onCallback: (PluginCallback) -> Unit,
) {
    private var props: JsonObject = JsonObject(emptyMap())
    private var bridgeFrame: JavaScriptReplyProxy? = null

    fun onMessage(
        data: String?,
        isMainFrame: Boolean,
        replyProxy: JavaScriptReplyProxy,
    ) {
        val message =
            try {
                Json.parseToJsonElement(data.orEmpty()).jsonObject
            } catch (error: Exception) {
                Timber.w(error, "Ignoring malformed plugin bridge message")
                return
            }

        when (message.string("type")) {
            "ready" ->
                if (isMainFrame) {
                    bridgeFrame = replyProxy
                    replyProxy.postMessage(
                        buildJsonObject {
                            put("type", "config")
                            put("pluginUrl", pluginUrl)
                            put("props", props)
                        }.toString(),
                    )
                }

            "api" -> answerApiCall(message, replyProxy)

            "callback" -> {
                val name = message.string("name") ?: return
                onCallback(PluginCallback(name, message["args"] ?: JsonNull))
            }
        }
    }

    /** Sets the props the plugin renders with, pushing them if the page is already running. */
    fun updateProps(newProps: JsonObject) {
        if (newProps == props) return
        props = newProps
        bridgeFrame?.postMessage(
            buildJsonObject {
                put("type", "props")
                put("props", newProps)
            }.toString(),
        )
    }

    private fun answerApiCall(
        message: JsonObject,
        replyProxy: JavaScriptReplyProxy,
    ) {
        val id = message["id"]?.jsonPrimitive?.intOrNull ?: return
        val request =
            ApiRequest(
                method = message.string("method") ?: "GET",
                url = message.string("url") ?: return,
                body = message.string("body"),
            )
        // Launched on the caller's (main) scope: the router moves to IO itself, and the reply proxy
        // must be called back on the UI thread.
        scope.launch {
            val response = router.handle(request)
            replyProxy.postMessage(
                buildJsonObject {
                    put("type", "api")
                    put("id", id)
                    put("status", response.status)
                    put("body", response.body)
                    put("contentType", response.contentType)
                }.toString(),
            )
        }
    }

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
}
