package org.dhis2.mobile.plugin.web

import androidx.webkit.JavaScriptReplyProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
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

            "api" -> answerApiCall(router, scope, message, replyProxy)

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
}
