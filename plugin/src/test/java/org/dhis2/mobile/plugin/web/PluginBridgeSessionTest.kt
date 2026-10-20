package org.dhis2.mobile.plugin.web

import androidx.webkit.JavaScriptReplyProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.dhis2.mobile.plugin.api.ApiRequest
import org.dhis2.mobile.plugin.api.ApiResponse
import org.dhis2.mobile.plugin.api.PluginApiRouter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PluginBridgeSessionTest {
    private val router: PluginApiRouter = mock()
    private val bridgeFrame: JavaScriptReplyProxy = mock()
    private val pluginFrame: JavaScriptReplyProxy = mock()
    private val callbacks = mutableListOf<PluginCallback>()

    private fun session(scope: CoroutineScope) =
        PluginBridgeSession(
            pluginUrl = "https://appassets.androidplatform.net/plugins/p-1/plugin.html",
            router = router,
            scope = scope,
            onCallback = { callbacks += it },
        )

    private fun sent(proxy: JavaScriptReplyProxy): JsonObject {
        val captor = argumentCaptor<String>()
        verify(proxy).postMessage(captor.capture())
        return Json.parseToJsonElement(captor.lastValue).jsonObject
    }

    @Test
    fun `answers the bridge page's ready message with the plugin url and props`() =
        runTest(UnconfinedTestDispatcher()) {
            val session = session(this)
            session.updateProps(JsonObject(mapOf("orgUnitId" to JsonPrimitive("ou1"))))

            session.onMessage("""{"type":"ready"}""", isMainFrame = true, replyProxy = bridgeFrame)

            val config = sent(bridgeFrame)
            assertEquals("config", config["type"]!!.jsonPrimitive.content)
            assertEquals(
                "https://appassets.androidplatform.net/plugins/p-1/plugin.html",
                config["pluginUrl"]!!.jsonPrimitive.content,
            )
            assertEquals("ou1", config["props"]!!.jsonObject["orgUnitId"]!!.jsonPrimitive.content)
        }

    @Test
    fun `replies to an api call on the frame that made it, matched by id`() =
        runTest(UnconfinedTestDispatcher()) {
            whenever(router.handle(ApiRequest("PUT", "/api/dataStore/ns/k", "{}"))) doReturn ApiResponse(200, """{"ok":1}""")

            session(this).onMessage(
                """{"type":"api","id":7,"method":"PUT","url":"/api/dataStore/ns/k","body":"{}"}""",
                isMainFrame = false,
                replyProxy = pluginFrame,
            )

            val reply = sent(pluginFrame)
            assertEquals(7, reply["id"]!!.jsonPrimitive.int)
            assertEquals(200, reply["status"]!!.jsonPrimitive.int)
            assertEquals("""{"ok":1}""", reply["body"]!!.jsonPrimitive.content)
        }

    @Test
    fun `forwards plugin callbacks to the host`() =
        runTest(UnconfinedTestDispatcher()) {
            session(this).onMessage(
                """{"type":"callback","name":"setFieldValue","args":{"fieldId":"de1","value":"3"}}""",
                isMainFrame = true,
                replyProxy = bridgeFrame,
            )

            val callback = callbacks.single()
            assertEquals("setFieldValue", callback.name)
            assertEquals(
                "de1",
                callback.args.jsonObject["fieldId"]!!
                    .jsonPrimitive.content,
            )
        }

    @Test
    fun `pushes new props to the bridge page once it is ready`() =
        runTest(UnconfinedTestDispatcher()) {
            val session = session(this)
            session.onMessage("""{"type":"ready"}""", isMainFrame = true, replyProxy = bridgeFrame)

            session.updateProps(JsonObject(mapOf("viewMode" to JsonPrimitive(true))))

            val captor = argumentCaptor<String>()
            verify(bridgeFrame, times(2)).postMessage(captor.capture())
            val props = Json.parseToJsonElement(captor.lastValue).jsonObject
            assertEquals("props", props["type"]!!.jsonPrimitive.content)
        }

    @Test
    fun `ignores ready messages from the plugin frame`() =
        runTest(UnconfinedTestDispatcher()) {
            session(this).onMessage("""{"type":"ready"}""", isMainFrame = false, replyProxy = pluginFrame)

            verify(pluginFrame, never()).postMessage(any<String>())
        }

    @Test
    fun `ignores malformed messages`() =
        runTest(UnconfinedTestDispatcher()) {
            session(this).onMessage("not json", isMainFrame = true, replyProxy = bridgeFrame)

            verify(bridgeFrame, never()).postMessage(any<String>())
            assertEquals(emptyList<PluginCallback>(), callbacks)
        }
}
