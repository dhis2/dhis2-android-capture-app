package org.dhis2.mobile.plugin.web

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.dhis2.mobile.plugin.domain.model.FormFieldPluginPlacement
import org.dhis2.mobile.plugin.domain.model.PluginField
import org.dhis2.mobile.plugin.domain.model.PluginFieldMetadata
import org.dhis2.mobile.plugin.domain.model.PluginFieldState
import org.dhis2.mobile.plugin.domain.model.PluginOption
import org.dhis2.mobile.plugin.domain.model.PluginOptionSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormFieldPluginPropsTest {
    private val placement =
        FormFieldPluginPlacement(
            formUid = "stage",
            elementId = "matrix",
            pluginSource = "/api/apps/matrix-select/plugin.html",
            fields = listOf(PluginField(uid = "deA", alias = "allergies"), PluginField(uid = "deB", alias = "penicillin")),
        )
    private val yesNo =
        PluginOptionSet(
            code = "YES_NONE_KNOWN",
            name = "Yes NoneKnown",
            options = listOf(PluginOption("o1", "YES", "Yes"), PluginOption("o2", "NONE_KNOWN", "None known")),
        )
    private val metadata =
        mapOf(
            "deA" to PluginFieldMetadata("Allergies", "Allergies", "Allergies?", "A", "TEXT", null, yesNo),
            "deB" to PluginFieldMetadata("Penicillin", "Penicillin", "Penicillin allergy", "B", "TEXT", null, yesNo),
        )
    private val states =
        mapOf(
            "deA" to PluginFieldState(value = "YES", error = "Check this", warning = null, editable = true, mandatory = true),
            "deB" to PluginFieldState(value = null, error = null, warning = null, editable = false, mandatory = false),
        )

    private fun props(): JsonObject = formFieldPluginProps(placement, states, metadata, orgUnitId = "ou", viewMode = false)

    @Test
    fun `values, errors and metadata are keyed by the alias configured for the plugin`() {
        val props = props()

        assertEquals("YES", props["values"]!!.jsonObject["allergies"]!!.jsonPrimitive.content)
        assertEquals(JsonNull, props["values"]!!.jsonObject["penicillin"])
        assertEquals(
            "Check this",
            props["errors"]!!
                .jsonObject["allergies"]!!
                .jsonArray
                .single()
                .jsonPrimitive.content,
        )
        assertEquals(listOf("allergies", "penicillin"), props["fieldsMetadata"]!!.jsonObject.keys.toList())
    }

    @Test
    fun `field metadata matches what the web Capture app sends`() {
        val allergies = props()["fieldsMetadata"]!!.jsonObject["allergies"]!!.jsonObject

        assertEquals("Allergies?", allergies["formName"]!!.jsonPrimitive.content)
        assertEquals("TEXT", allergies["type"]!!.jsonPrimitive.content)
        assertTrue(allergies["compulsory"]!!.jsonPrimitive.boolean)
        val penicillin = props()["fieldsMetadata"]!!.jsonObject["penicillin"]!!.jsonObject
        assertTrue(penicillin["disabled"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `options carry code, value, text and name, as Capture's own option objects do`() {
        val option =
            props()["fieldsMetadata"]!!
                .jsonObject["allergies"]!!
                .jsonObject["optionSet"]!!
                .jsonObject["options"]!!
                .jsonArray
                .first()
                .jsonObject

        assertEquals("YES", option["code"]!!.jsonPrimitive.content)
        assertEquals("YES", option["value"]!!.jsonPrimitive.content)
        assertEquals("Yes", option["text"]!!.jsonPrimitive.content)
        assertEquals("Yes", option["name"]!!.jsonPrimitive.content)
    }

    @Test
    fun `the stored values replace the values the form displays`() {
        // The form shows an option's name; a plugin, as on the web, gets the stored option code.
        val displayed = mapOf("deA" to states.getValue("deA").copy(value = "Yes"))

        val merged = displayed.withStoredValues(mapOf("deA" to "YES"))

        assertEquals("YES", merged.getValue("deA").value)
        assertEquals("Check this", merged.getValue("deA").error)
    }

    @Test
    fun `a field with no stored value has none, whatever the form displays`() {
        val merged = mapOf("deA" to states.getValue("deA")).withStoredValues(emptyMap())

        assertNull(merged.getValue("deA").value)
    }

    @Test
    fun `setFieldValue on an alias writes the field it maps to`() {
        val args =
            buildJsonObject {
                put("fieldId", "penicillin")
                put("value", "NONE_KNOWN")
            }

        assertEquals("deB" to "NONE_KNOWN", formFieldPluginWrite(placement, PluginCallback("setFieldValue", args)))
    }

    @Test
    fun `a write to a field the plugin was not given is refused`() {
        val args =
            buildJsonObject {
                put("fieldId", "deA")
                put("value", "YES")
            }

        // The field's own uid is not an alias: plugins only reach fields through their field map.
        assertNull(formFieldPluginWrite(placement, PluginCallback("setFieldValue", args)))
    }

    @Test
    fun `clearing a value writes no value`() {
        val args =
            buildJsonObject {
                put("fieldId", "allergies")
                put("value", JsonNull)
            }

        assertEquals("deA" to null, formFieldPluginWrite(placement, PluginCallback("setFieldValue", args)))
    }

    @Test
    fun `other callbacks write nothing`() {
        assertNull(formFieldPluginWrite(placement, PluginCallback("onError", JsonPrimitive("boom"))))
    }
}
