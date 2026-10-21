package org.dhis2.mobile.plugin.web

import androidx.webkit.JavaScriptReplyProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.dhis2.mobile.plugin.api.ApiRequest
import org.dhis2.mobile.plugin.api.PluginApiRouter

/**
 * Answers an `api` message from `api-shim.js`: a captured `/api/` call, replied to on the same
 * frame with the router's response under the same `id`, so concurrent calls resolve independently.
 */
internal fun answerApiCall(
    router: PluginApiRouter,
    scope: CoroutineScope,
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

internal fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
