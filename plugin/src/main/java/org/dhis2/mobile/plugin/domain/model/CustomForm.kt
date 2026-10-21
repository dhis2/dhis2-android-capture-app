package org.dhis2.mobile.plugin.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private const val CUSTOM_FORM_TYPE = "CUSTOM"

/** The data set instance a custom form is rendered for. */
data class CustomFormContext(
    val dataSetUid: String,
    val periodId: String,
    val orgUnitUid: String,
    val attributeOptionComboUid: String,
)

/** A data element of the data set, with what the form needs to render its inputs. */
data class CustomFormDataElement(
    val uid: String,
    val name: String,
    val valueType: String,
    val options: List<CustomFormOption>,
)

data class CustomFormOption(
    val code: String,
    val name: String,
)

/** What a custom form renders with: its HTML, and the instance's metadata, values and indicators. */
data class CustomFormData(
    val html: String,
    val dataElements: List<CustomFormDataElement>,
    val values: Map<CustomFormFieldId, String>,
    val indicators: Map<String, String>,
)

/** A field of a custom form: the input `{dataElement}-{categoryOptionCombo}-val` in its HTML. */
data class CustomFormFieldId(
    val dataElement: String,
    val categoryOptionCombo: String,
) {
    val key: String get() = "$dataElement-$categoryOptionCombo"
}

/**
 * The custom form HTML of each data set in an `/api/dataSets?fields=id,formType,dataEntryForm[htmlCode]`
 * response, keyed by data set uid. A data set whose form type is not `CUSTOM` keeps a form it no
 * longer uses, so only `CUSTOM` ones count, as on the web.
 */
fun parseCustomForms(response: String): Map<String, String> =
    ((Json.parseToJsonElement(response) as? JsonObject)?.get("dataSets") as? JsonArray)
        .orEmpty()
        .mapNotNull { it as? JsonObject }
        .filter { it.string("formType") == CUSTOM_FORM_TYPE }
        .mapNotNull { dataSet ->
            val uid = dataSet.string("id") ?: return@mapNotNull null
            val html = (dataSet["dataEntryForm"] as? JsonObject)?.string("htmlCode")
            html?.takeIf { it.isNotBlank() }?.let { uid to it }
        }.toMap()

private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
