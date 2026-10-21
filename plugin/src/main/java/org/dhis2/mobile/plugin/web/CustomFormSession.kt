package org.dhis2.mobile.plugin.web

import androidx.webkit.JavaScriptReplyProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.dhis2.mobile.plugin.api.PluginApiRouter
import org.dhis2.mobile.plugin.domain.model.CustomFormContext
import org.dhis2.mobile.plugin.domain.model.CustomFormData
import org.dhis2.mobile.plugin.domain.model.CustomFormFieldId
import timber.log.Timber

/**
 * The app's end of the `dhis2Native` message bridge for one rendered data set custom form.
 *
 * Messages from `custom-form.html`:
 * - `ready`: answered with a `form` message carrying the HTML, the instance's context, the data
 *   set's data elements, the stored values and the indicators.
 * - `setValue`: a field the user changed, saved through [save] and answered with `saved` under the
 *   same `id`, with the recalculated indicators or the reason the value was rejected. Only data
 *   elements of the data set can be written.
 * - `api`: a captured `/api/` call from the form's own scripts, answered by the router.
 */
internal class CustomFormSession(
    private val context: CustomFormContext,
    private val load: suspend () -> Result<CustomFormData>,
    private val save: suspend (CustomFormFieldId, String?) -> Result<Map<String, String>>,
    private val router: PluginApiRouter,
    private val scope: CoroutineScope,
) {
    private var page: JavaScriptReplyProxy? = null
    private var editable = false
    private var dataElements: Set<String> = emptySet()

    fun onMessage(
        data: String?,
        isMainFrame: Boolean,
        replyProxy: JavaScriptReplyProxy,
    ) {
        val message =
            try {
                Json.parseToJsonElement(data.orEmpty()).jsonObject
            } catch (error: Exception) {
                Timber.w(error, "Ignoring malformed custom form bridge message")
                return
            }

        when (message.string("type")) {
            "ready" -> if (isMainFrame) sendForm(replyProxy)
            "setValue" -> if (isMainFrame) setValue(message, replyProxy)
            "api" -> answerApiCall(router, scope, message, replyProxy)
        }
    }

    /** Whether the user can change values: not once the instance is completed or out of their scope. */
    fun updateEditable(newEditable: Boolean) {
        if (newEditable == editable) return
        editable = newEditable
        page?.postMessage(
            buildJsonObject {
                put("type", "editable")
                put("editable", newEditable)
            }.toString(),
        )
    }

    private fun sendForm(replyProxy: JavaScriptReplyProxy) {
        page = replyProxy
        scope.launch {
            val form =
                load().getOrElse { error ->
                    Timber.e(error, "Could not load the custom form of %s", context.dataSetUid)
                    replyProxy.postMessage(
                        buildJsonObject {
                            put("type", "error")
                            put("message", error.message ?: "The form could not be loaded")
                        }.toString(),
                    )
                    return@launch
                }
            dataElements = form.dataElements.map { it.uid }.toSet()
            replyProxy.postMessage(formMessage(form).toString())
        }
    }

    private fun setValue(
        message: JsonObject,
        replyProxy: JavaScriptReplyProxy,
    ) {
        val id = message["id"]?.jsonPrimitive?.intOrNull ?: return
        val dataElement = message.string("dataElement")
        val categoryOptionCombo = message.string("categoryOptionCombo")
        if (dataElement == null || categoryOptionCombo == null || dataElement !in dataElements) {
            Timber.w("Custom form write to %s refused: not a data element of %s", dataElement, context.dataSetUid)
            replyProxy.postMessage(saved(id, Result.failure(IllegalArgumentException("Not a field of this form"))).toString())
            return
        }
        val value = message.string("value")?.takeIf { it.isNotEmpty() }
        scope.launch {
            val result = save(CustomFormFieldId(dataElement, categoryOptionCombo), value)
            replyProxy.postMessage(saved(id, result).toString())
        }
    }

    private fun formMessage(form: CustomFormData) =
        buildJsonObject {
            put("type", "form")
            put("html", form.html)
            putJsonObject("context") {
                put("dataSet", context.dataSetUid)
                put("period", context.periodId)
                put("orgUnit", context.orgUnitUid)
                put("attributeOptionCombo", context.attributeOptionComboUid)
            }
            putJsonObject("dataElements") {
                form.dataElements.forEach { dataElement ->
                    putJsonObject(dataElement.uid) {
                        put("name", dataElement.name)
                        put("valueType", dataElement.valueType)
                        put(
                            "options",
                            buildJsonArray {
                                dataElement.options.forEach { option ->
                                    add(
                                        buildJsonObject {
                                            put("code", option.code)
                                            put("name", option.name)
                                        },
                                    )
                                }
                            },
                        )
                    }
                }
            }
            putJsonObject("values") { form.values.forEach { (field, value) -> put(field.key, value) } }
            putJsonObject("indicators") { form.indicators.forEach { (uid, value) -> put(uid, value) } }
            put("editable", editable)
        }

    private fun saved(
        id: Int,
        result: Result<Map<String, String>>,
    ) = buildJsonObject {
        put("type", "saved")
        put("id", id)
        put("ok", result.isSuccess)
        result
            .onSuccess { indicators -> putJsonObject("indicators") { indicators.forEach { (uid, value) -> put(uid, value) } } }
            .onFailure { error -> put("error", error.message ?: "The value could not be saved") }
    }
}
