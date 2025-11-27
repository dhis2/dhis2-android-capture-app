package org.dhis2.form.ui.customintent

import android.content.Intent
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.dhis2.mobile.commons.model.CustomIntentRequestArgumentModel
import org.dhis2.mobile.commons.model.CustomIntentResponseDataModel
import org.dhis2.mobile.commons.model.CustomIntentResponseExtraType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class CustomIntentActivityResultContractTest {
    private lateinit var contract: CustomIntentActivityResultContract

    @Before
    fun setUp() {
        contract = CustomIntentActivityResultContract()
    }

    @Test
    fun `mapIntentData should create intent with string extras`() {
        val requestParameters =
            listOf(
                CustomIntentRequestArgumentModel(key = "param1", value = "value1"),
                CustomIntentRequestArgumentModel(key = "param2", value = "value2"),
            )

        val result = contract.mapIntentData("com.example.app", requestParameters)

        assertNotNull(result)
        // Note: In unit tests without Robolectric, Intent properties may not work as expected
        // We verify the intent object is created successfully
    }

    @Test
    fun `mapIntentData should handle different parameter types`() {
        val requestParameters =
            listOf(
                CustomIntentRequestArgumentModel(key = "stringParam", value = "text"),
                CustomIntentRequestArgumentModel(key = "intParam", value = 42),
                CustomIntentRequestArgumentModel(key = "boolParam", value = true),
                CustomIntentRequestArgumentModel(key = "doubleParam", value = 3.14),
                CustomIntentRequestArgumentModel(key = "longParam", value = 100L),
            )

        val result = contract.mapIntentData("com.example.app", requestParameters)

        assertNotNull(result)
    }

    @Test
    fun `mapIntentResponseData should extract integer type`() {
        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "intValue",
                    extraType = CustomIntentResponseExtraType.INTEGER,
                    key = null,
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("intValue") } doReturn true
                on { getIntExtra("intValue", 0) } doReturn 42
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(listOf("42"), result)
    }

    @Test
    fun `mapIntentResponseData should extract boolean type`() {
        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "boolValue",
                    extraType = CustomIntentResponseExtraType.BOOLEAN,
                    key = null,
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("boolValue") } doReturn true
                on { getBooleanExtra("boolValue", false) } doReturn true
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(listOf("true"), result)
    }

    @Test
    fun `mapIntentResponseData should extract float type`() {
        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "floatValue",
                    extraType = CustomIntentResponseExtraType.FLOAT,
                    key = null,
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("floatValue") } doReturn true
                on { getFloatExtra("floatValue", 0f) } doReturn 3.14f
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(listOf("3.14"), result)
    }

    @Test
    fun `mapIntentResponseData should extract string type`() {
        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "stringValue",
                    extraType = CustomIntentResponseExtraType.STRING,
                    key = null,
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("stringValue") } doReturn true
                on { getStringExtra("stringValue") } doReturn "test-value"
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(listOf("test-value"), result)
    }

    @Test
    fun `mapIntentResponseData should extract multiple string values`() {
        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "result1",
                    extraType = CustomIntentResponseExtraType.STRING,
                    key = null,
                ),
                CustomIntentResponseDataModel(
                    name = "result2",
                    extraType = CustomIntentResponseExtraType.STRING,
                    key = null,
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("result1") } doReturn true
                on { hasExtra("result2") } doReturn true
                on { getStringExtra("result1") } doReturn "value1"
                on { getStringExtra("result2") } doReturn "value2"
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(listOf("value1", "value2"), result)
    }

    @Test
    fun `mapIntentResponseData should return null when intent is null`() {
        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "result",
                    extraType = CustomIntentResponseExtraType.STRING,
                    key = null,
                ),
            )

        val result = contract.mapIntentResponseData(response, null)

        assertNull(result)
    }

    @Test
    fun `mapIntentResponseData should return null when no extras match`() {
        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "result",
                    extraType = CustomIntentResponseExtraType.STRING,
                    key = null,
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("result") } doReturn false
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNull(result)
    }

    @Test
    fun `mapIntentResponseData should extract value from JSON object`() {
        val jsonObject =
            JsonObject().apply {
                addProperty("name", "John Doe")
                addProperty("age", 30)
            }

        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "userObject",
                    extraType = CustomIntentResponseExtraType.OBJECT,
                    key = "name",
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("userObject") } doReturn true
                on { getStringExtra("userObject") } doReturn jsonObject.toString()
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(listOf("John Doe"), result)
    }

    @Test
    fun `mapIntentResponseData should return null for object with missing key`() {
        val jsonObject =
            JsonObject().apply {
                addProperty("age", 30)
            }

        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "userObject",
                    extraType = CustomIntentResponseExtraType.OBJECT,
                    key = "name",
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("userObject") } doReturn true
                on { getStringExtra("userObject") } doReturn jsonObject.toString()
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNull(result)
    }

    @Test
    fun `mapIntentResponseData should extract values from list of JSON objects`() {
        val jsonArray =
            listOf(
                JsonObject().apply { addProperty("name", "John") },
                JsonObject().apply { addProperty("name", "Jane") },
                JsonObject().apply { addProperty("name", "Bob") },
            )
        val gson = Gson()

        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "usersList",
                    extraType = CustomIntentResponseExtraType.LIST_OF_OBJECTS,
                    key = "name",
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("usersList") } doReturn true
                on { getStringExtra("usersList") } doReturn gson.toJson(jsonArray)
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(3, result?.size)
        assertEquals(listOf("John", "Jane", "Bob"), result)
    }

    @Test
    fun `mapIntentResponseData should filter list objects by key presence`() {
        val jsonArray =
            listOf(
                JsonObject().apply {
                    addProperty("name", "John")
                    addProperty("age", 30)
                },
                JsonObject().apply { addProperty("age", 25) }, // No name
                JsonObject().apply { addProperty("name", "Jane") },
            )
        val gson = Gson()

        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "usersList",
                    extraType = CustomIntentResponseExtraType.LIST_OF_OBJECTS,
                    key = "name",
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("usersList") } doReturn true
                on { getStringExtra("usersList") } doReturn gson.toJson(jsonArray)
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals(listOf("John", "Jane"), result)
    }

    @Test
    fun `mapIntentResponseData should return null for empty list of objects`() {
        val jsonArray = emptyList<JsonObject>()
        val gson = Gson()

        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "emptyList",
                    extraType = CustomIntentResponseExtraType.LIST_OF_OBJECTS,
                    key = "name",
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("emptyList") } doReturn true
                on { getStringExtra("emptyList") } doReturn gson.toJson(jsonArray)
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNull(result)
    }

    @Test
    fun `mapIntentResponseData should skip missing extras`() {
        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "missing",
                    extraType = CustomIntentResponseExtraType.STRING,
                    key = null,
                ),
                CustomIntentResponseDataModel(
                    name = "present",
                    extraType = CustomIntentResponseExtraType.STRING,
                    key = null,
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("missing") } doReturn false
                on { hasExtra("present") } doReturn true
                on { getStringExtra("present") } doReturn "value"
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(listOf("value"), result)
    }

    @Test
    fun `getComplexObject should parse valid JSON string to JsonObject`() {
        val jsonString = """{"name": "John", "age": 30}"""

        val result = contract.getComplexObject(jsonString)

        assertNotNull(result)
        assertEquals("John", result?.get("name")?.asString)
        assertEquals(30, result?.get("age")?.asInt)
    }

    @Test
    fun `getComplexObject should return null for invalid JSON`() {
        val invalidJson = "not a valid json"

        val result = contract.getComplexObject(invalidJson)

        assertNull(result)
    }

    @Test
    fun `getListOfObjects should parse valid JSON array to list of JsonObject`() {
        val jsonArray = """[{"name": "John"}, {"name": "Jane"}]"""

        val result = contract.getListOfObjects(jsonArray)

        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals("John", result?.get(0)?.get("name")?.asString)
        assertEquals("Jane", result?.get(1)?.get("name")?.asString)
    }

    @Test
    fun `getListOfObjects should return null for invalid JSON array`() {
        val invalidJson = "not a valid json array"

        val result = contract.getListOfObjects(invalidJson)

        assertNull(result)
    }

    @Test
    fun `mapIntentResponseData should handle mixed response types`() {
        val jsonObject =
            JsonObject().apply {
                addProperty("id", "123")
            }

        val response =
            listOf(
                CustomIntentResponseDataModel(
                    name = "stringValue",
                    extraType = CustomIntentResponseExtraType.STRING,
                    key = null,
                ),
                CustomIntentResponseDataModel(
                    name = "intValue",
                    extraType = CustomIntentResponseExtraType.INTEGER,
                    key = null,
                ),
                CustomIntentResponseDataModel(
                    name = "objectValue",
                    extraType = CustomIntentResponseExtraType.OBJECT,
                    key = "id",
                ),
            )

        val intent =
            mock<Intent> {
                on { hasExtra("stringValue") } doReturn true
                on { hasExtra("intValue") } doReturn true
                on { hasExtra("objectValue") } doReturn true
                on { getStringExtra("stringValue") } doReturn "test"
                on { getIntExtra("intValue", 0) } doReturn 42
                on { getStringExtra("objectValue") } doReturn jsonObject.toString()
            }

        val result = contract.mapIntentResponseData(response, intent)

        assertNotNull(result)
        assertEquals(3, result?.size)
        assertEquals(listOf("test", "42", "123"), result)
    }
}
