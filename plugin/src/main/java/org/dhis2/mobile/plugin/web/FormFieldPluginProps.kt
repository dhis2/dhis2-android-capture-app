package org.dhis2.mobile.plugin.web

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import org.dhis2.mobile.plugin.domain.model.FormFieldPluginPlacement
import org.dhis2.mobile.plugin.domain.model.PluginFieldMetadata
import org.dhis2.mobile.plugin.domain.model.PluginFieldState
import org.dhis2.mobile.plugin.domain.model.PluginOptionSet

private const val SET_FIELD_VALUE = "setFieldValue"

/**
 * The props a DHIS2 Capture form field plugin receives, shaped as the web Capture app sends them:
 * everything is keyed by the alias configured for the plugin (`IdFromPlugin`), never by uid, and only
 * the fields in its field map are included.
 */
fun formFieldPluginProps(
    placement: FormFieldPluginPlacement,
    states: Map<String, PluginFieldState>,
    metadata: Map<String, PluginFieldMetadata>,
    orgUnitId: String?,
    viewMode: Boolean,
): JsonObject {
    val fields = placement.fields
    return buildJsonObject {
        put("orgUnitId", orgUnitId)
        put("viewMode", viewMode)
        put("formSubmitted", false)
        put("values", JsonObject(fields.associate { it.alias to JsonPrimitive(states[it.uid]?.value) }))
        put("errors", messages(fields.associate { it.alias to states[it.uid]?.error }))
        put("warnings", messages(fields.associate { it.alias to states[it.uid]?.warning }))
        put(
            "fieldsMetadata",
            JsonObject(
                fields
                    .mapNotNull { field ->
                        metadata[field.uid]?.let { field.alias to it.toProps(states[field.uid]) }
                    }.toMap(),
            ),
        )
    }
}

/**
 * The field write a plugin callback stands for, as `uid to value`, or null if it writes nothing. A
 * plugin reaches fields only through its aliases, so naming any other field is refused.
 */
fun formFieldPluginWrite(
    placement: FormFieldPluginPlacement,
    callback: PluginCallback,
): Pair<String, String?>? {
    if (callback.name != SET_FIELD_VALUE) return null
    val args = callback.args as? JsonObject ?: return null
    val alias = (args["fieldId"] as? JsonPrimitive)?.contentOrNull ?: return null
    val field = placement.fields.firstOrNull { it.alias == alias } ?: return null
    val value =
        when (val raw = args["value"]) {
            is JsonPrimitive -> raw.contentOrNull
            null -> null
            else -> raw.toString()
        }
    return field.uid to value
}

private fun messages(byAlias: Map<String, String?>) =
    JsonObject(byAlias.mapNotNull { (alias, message) -> message?.let { alias to JsonArray(listOf(JsonPrimitive(it))) } }.toMap())

private fun PluginFieldMetadata.toProps(state: PluginFieldState?) =
    buildJsonObject {
        put("name", name)
        put("shortName", shortName)
        put("formName", formName)
        put("code", code)
        put("description", description)
        put("type", valueType)
        put("compulsory", state?.mandatory == true)
        put("disabled", state?.editable == false)
        put("displayInForms", true)
        put("displayInReports", true)
        optionSet?.let { put("optionSet", it.toProps()) }
    }

/** Options carry `code` and `value`, `text` and `name`: Capture's own option objects use both pairs. */
private fun PluginOptionSet.toProps() =
    buildJsonObject {
        put("code", code)
        put("name", name)
        put(
            "options",
            buildJsonArray {
                options.forEach { option ->
                    add(
                        buildJsonObject {
                            put("id", option.uid)
                            put("code", option.code)
                            put("value", option.code)
                            put("text", option.name)
                            put("name", option.name)
                        },
                    )
                }
            },
        )
    }

/**
 * These field states with each field's stored value. The form displays option names and formatted
 * values; a plugin, as on the web, works with what is stored: option codes and raw values.
 */
fun Map<String, PluginFieldState>.withStoredValues(stored: Map<String, String?>): Map<String, PluginFieldState> =
    mapValues { (uid, state) -> state.copy(value = stored[uid]) }
