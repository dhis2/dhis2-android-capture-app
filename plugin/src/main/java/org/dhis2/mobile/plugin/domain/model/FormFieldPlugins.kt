package org.dhis2.mobile.plugin.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import timber.log.Timber

/** A form field a plugin may read and write, and the name the plugin knows it by. */
data class PluginField(
    val uid: String,
    val alias: String,
)

/**
 * A DHIS2 Capture form field plugin placed in a form, as configured in the `capture/dataEntryForms`
 * dataStore key — the same configuration the web Capture app renders, written by the Tracker Plugin
 * Configurator.
 *
 * @property formUid The form it belongs to: a program stage for event forms, a program for
 *   registration forms, a tracked entity type for registration without a program.
 * @property pluginSource The installed app serving the plugin, `{server}/api/apps/{key}/plugin.html`.
 * @property fields The fields it may read and write (its `fieldMap`), in configured order.
 */
data class FormFieldPluginPlacement(
    val formUid: String,
    val elementId: String,
    val pluginSource: String,
    val fields: List<PluginField>,
) {
    val appKey: String? get() = pluginAppKey(pluginSource)
}

/**
 * A form's layout from `capture/dataEntryForms`: when a form has one, the web Capture app renders
 * exactly these sections and elements, in this order, and so does this app.
 *
 * @property formUid The program stage, program or tracked entity type it applies to.
 */
data class FormLayout(
    val formUid: String,
    val sections: List<FormLayoutSection>,
)

/**
 * @property id A program stage section uid, or an id only the configuration knows.
 * @property name Title for a section only the configuration knows; the Tracker Plugin Configurator
 *   leaves it out for native sections.
 */
data class FormLayoutSection(
    val id: String,
    val name: String?,
    val elements: List<FormLayoutElement>,
)

sealed interface FormLayoutElement {
    /** A data element or tracked entity attribute rendered natively. */
    data class Field(
        val uid: String,
    ) : FormLayoutElement

    data class Plugin(
        val placement: FormFieldPluginPlacement,
    ) : FormLayoutElement
}

/** Every plugin placed in these layouts. */
fun List<FormLayout>.placements(): List<FormFieldPluginPlacement> =
    flatMap { layout -> layout.sections.flatMap { it.elements } }.filterIsInstance<FormLayoutElement.Plugin>().map { it.placement }

/** An app installed on the server, as `/api/apps` describes it. */
data class PluginApp(
    val key: String,
    val name: String,
    val version: String,
    val appHubId: String?,
)

private val INSTALLED_APP_PATH = Regex("/api/apps/([^/?#]+)/")
private const val PLUGIN_ELEMENT = "plugin"
private const val METADATA_ATTRIBUTE = "Attribute"

/**
 * The installed-app key a plugin source points at, or null if it is not served by an installed app
 * (a developer's local `d2-app-scripts start` URL, for instance), in which case there is no bundle
 * to download. Only the path is read, so the host the configuration was written with does not matter.
 */
fun pluginAppKey(pluginSource: String): String? = INSTALLED_APP_PATH.find(pluginSource)?.groupValues?.get(1)

/**
 * The form layouts in a `capture/dataEntryForms` value. Each form is read on its own, so one
 * malformed entry costs only its own form.
 */
fun parseDataEntryForms(json: String): List<FormLayout> {
    val forms =
        try {
            Json.parseToJsonElement(json) as? JsonObject
        } catch (error: Exception) {
            Timber.w(error, "Unreadable capture/dataEntryForms configuration")
            null
        } ?: return emptyList()

    return forms.mapNotNull { (formUid, sections) ->
        val sectionList = sections as? JsonArray ?: return@mapNotNull null
        FormLayout(
            formUid = formUid,
            sections =
                sectionList.mapNotNull { section ->
                    val sectionObject = section as? JsonObject ?: return@mapNotNull null
                    FormLayoutSection(
                        id = sectionObject.string("id") ?: return@mapNotNull null,
                        name = sectionObject.string("name"),
                        elements =
                            (sectionObject["elements"] as? JsonArray)
                                .orEmpty()
                                .mapNotNull { (it as? JsonObject)?.toElement(formUid) },
                    )
                },
        )
    }
}

private fun JsonObject.toElement(formUid: String): FormLayoutElement? =
    if (string("type") == PLUGIN_ELEMENT) {
        toPlacement(formUid)?.let(FormLayoutElement::Plugin)
    } else {
        string("id")?.let(FormLayoutElement::Field)
    }

private fun JsonObject.toPlacement(formUid: String): FormFieldPluginPlacement? {
    val source = string("pluginSource") ?: return null
    val fields =
        (this["fieldMap"] as? JsonArray)
            .orEmpty()
            .mapNotNull { it as? JsonObject }
            // `Attribute` entries map metadata attributes, not form fields.
            .filter { it.string("objectType") != METADATA_ATTRIBUTE }
            .mapNotNull { mapping ->
                val uid = mapping.string("IdFromApp") ?: return@mapNotNull null
                val alias = mapping.string("IdFromPlugin") ?: return@mapNotNull null
                PluginField(uid, alias)
            }
    return FormFieldPluginPlacement(formUid, string("id") ?: source, source, fields)
}

private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
