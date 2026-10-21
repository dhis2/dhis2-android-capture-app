package org.dhis2.mobile.plugin.web

import androidx.webkit.JavaScriptReplyProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.dhis2.mobile.plugin.api.ApiRequest
import org.dhis2.mobile.plugin.api.ApiResponse
import org.dhis2.mobile.plugin.api.PluginApiRouter
import org.dhis2.mobile.plugin.domain.model.CustomFormContext
import org.dhis2.mobile.plugin.domain.model.CustomFormData
import org.dhis2.mobile.plugin.domain.model.CustomFormDataElement
import org.dhis2.mobile.plugin.domain.model.CustomFormFieldId
import org.dhis2.mobile.plugin.domain.model.CustomFormOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CustomFormSessionTest {
    private val router: PluginApiRouter = mock()
    private val page: JavaScriptReplyProxy = mock()
    private val saved = mutableListOf<Pair<CustomFormFieldId, String?>>()

    private val context = CustomFormContext("TuL8IOPzpHh", "202610", "DiszpKrYNg8", "HllvX50cXC0")
    private val form =
        CustomFormData(
            html = "<h2>EPI Stock</h2>",
            dataElements =
                listOf(
                    CustomFormDataElement("t99PL3gUxIl", "BCG doses", "INTEGER_ZERO_OR_POSITIVE", emptyList()),
                    CustomFormDataElement("vaccine0001", "Vaccine", "TEXT", listOf(CustomFormOption("BCG", "BCG vaccine"))),
                ),
            values = mapOf(CustomFormFieldId("t99PL3gUxIl", "HllvX50cXC0") to "12"),
            indicators = mapOf("Uvn6LCg7dVU" to "4.0"),
        )

    private fun session(
        scope: CoroutineScope,
        saveResult: Result<Map<String, String>> = Result.success(mapOf("Uvn6LCg7dVU" to "5.0")),
    ) = CustomFormSession(
        context = context,
        load = { Result.success(form) },
        save = { field, value ->
            saved += field to value
            saveResult
        },
        router = router,
        scope = scope,
    )

    private fun sent(): JsonObject {
        val captor = argumentCaptor<String>()
        verify(page, atLeastOnce()).postMessage(captor.capture())
        return Json.parseToJsonElement(captor.lastValue).jsonObject
    }

    @Test
    fun `answers ready with the form, its context, metadata, values and indicators`() =
        runTest(UnconfinedTestDispatcher()) {
            val session = session(this)
            session.updateEditable(true)

            session.onMessage("""{"type":"ready"}""", isMainFrame = true, replyProxy = page)

            val message = sent()
            assertEquals("form", message["type"]!!.jsonPrimitive.content)
            assertEquals("<h2>EPI Stock</h2>", message["html"]!!.jsonPrimitive.content)
            assertEquals("TuL8IOPzpHh", message["context"]!!.jsonObject["dataSet"]!!.jsonPrimitive.content)
            assertEquals("202610", message["context"]!!.jsonObject["period"]!!.jsonPrimitive.content)
            assertEquals("12", message["values"]!!.jsonObject["t99PL3gUxIl-HllvX50cXC0"]!!.jsonPrimitive.content)
            assertEquals("4.0", message["indicators"]!!.jsonObject["Uvn6LCg7dVU"]!!.jsonPrimitive.content)
            val vaccine = message["dataElements"]!!.jsonObject["vaccine0001"]!!.jsonObject
            assertEquals("TEXT", vaccine["valueType"]!!.jsonPrimitive.content)
            assertEquals(
                "BCG",
                vaccine["options"]!!
                    .jsonArray[0]
                    .jsonObject["code"]!!
                    .jsonPrimitive.content,
            )
            assertTrue(message["editable"]!!.jsonPrimitive.boolean)
        }

    @Test
    fun `saves a value and replies with the recalculated indicators`() =
        runTest(UnconfinedTestDispatcher()) {
            val session = session(this)
            session.onMessage("""{"type":"ready"}""", isMainFrame = true, replyProxy = page)

            session.onMessage(
                """{"type":"setValue","id":3,"dataElement":"t99PL3gUxIl","categoryOptionCombo":"HllvX50cXC0","value":"15"}""",
                isMainFrame = true,
                replyProxy = page,
            )

            assertEquals(listOf(CustomFormFieldId("t99PL3gUxIl", "HllvX50cXC0") to "15"), saved)
            val reply = sent()
            assertEquals("saved", reply["type"]!!.jsonPrimitive.content)
            assertEquals(3, reply["id"]!!.jsonPrimitive.int)
            assertTrue(reply["ok"]!!.jsonPrimitive.boolean)
            assertEquals("5.0", reply["indicators"]!!.jsonObject["Uvn6LCg7dVU"]!!.jsonPrimitive.content)
        }

    @Test
    fun `an empty value clears the field`() =
        runTest(UnconfinedTestDispatcher()) {
            val session = session(this)
            session.onMessage("""{"type":"ready"}""", isMainFrame = true, replyProxy = page)

            session.onMessage(
                """{"type":"setValue","id":4,"dataElement":"t99PL3gUxIl","categoryOptionCombo":"HllvX50cXC0","value":""}""",
                isMainFrame = true,
                replyProxy = page,
            )

            assertEquals(listOf(CustomFormFieldId("t99PL3gUxIl", "HllvX50cXC0") to null), saved)
        }

    @Test
    fun `a rejected value is reported back to the form`() =
        runTest(UnconfinedTestDispatcher()) {
            val session = session(this, saveResult = Result.failure(IllegalArgumentException("Not a positive integer")))
            session.onMessage("""{"type":"ready"}""", isMainFrame = true, replyProxy = page)

            session.onMessage(
                """{"type":"setValue","id":5,"dataElement":"t99PL3gUxIl","categoryOptionCombo":"HllvX50cXC0","value":"-1"}""",
                isMainFrame = true,
                replyProxy = page,
            )

            val reply = sent()
            assertFalse(reply["ok"]!!.jsonPrimitive.boolean)
            assertEquals("Not a positive integer", reply["error"]!!.jsonPrimitive.content)
        }

    @Test
    fun `refuses to write a data element that is not in the data set`() =
        runTest(UnconfinedTestDispatcher()) {
            val session = session(this)
            session.onMessage("""{"type":"ready"}""", isMainFrame = true, replyProxy = page)

            session.onMessage(
                """{"type":"setValue","id":6,"dataElement":"otherDE0001","categoryOptionCombo":"HllvX50cXC0","value":"1"}""",
                isMainFrame = true,
                replyProxy = page,
            )

            assertTrue(saved.isEmpty())
            assertFalse(sent()["ok"]!!.jsonPrimitive.boolean)
        }

    @Test
    fun `routes the form's own api calls to the router`() =
        runTest(UnconfinedTestDispatcher()) {
            whenever(router.handle(ApiRequest("GET", "/api/me", null))) doReturn ApiResponse(200, """{"id":"u1"}""")

            session(this).onMessage("""{"type":"api","id":9,"method":"GET","url":"/api/me"}""", isMainFrame = true, replyProxy = page)

            val reply = sent()
            assertEquals("api", reply["type"]!!.jsonPrimitive.content)
            assertEquals(200, reply["status"]!!.jsonPrimitive.int)
        }
}
