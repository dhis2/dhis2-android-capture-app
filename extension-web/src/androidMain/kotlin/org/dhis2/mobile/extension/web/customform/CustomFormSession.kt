package org.dhis2.mobile.extension.web.customform

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
import org.dhis2.mobile.extension.host.model.DataSetInstanceRef
import org.dhis2.mobile.extension.web.api.ApiRequest
import org.dhis2.mobile.extension.web.api.ApiResponse
import org.dhis2.mobile.extension.web.bridge.answerApiCall
import org.dhis2.mobile.extension.web.bridge.string
import timber.log.Timber

/**
 * The app's end of the `dhis2Native` message bridge for one rendered data set custom form.
 *
 * Messages from `custom-form.html`:
 * - `ready`: answered with a `form` message carrying the HTML, the instance's context, the data
 *   set's data elements, the stored values and the indicators.
 * - `rendered`: the form finished loading; until then the renderer may time out.
 * - `setValue`: a field the user changed, saved through [save] and answered with `saved` under the
 *   same `id`, with the recalculated indicators or the reason the value was rejected. Only data
 *   elements of the data set can be written; this is checked here, in Kotlin.
 * - `scriptError`: an uncaught error in the form's scripts. Reported; the form stays.
 * - `api`: a captured `/api/` call from the form's own scripts, answered by [answerApi].
 *
 * @param save The data set screen's own save. Returns the recalculated indicators.
 */
internal class CustomFormSession(
    private val instance: DataSetInstanceRef,
    private val load: suspend () -> Result<CustomFormData>,
    private val save: suspend (CustomFormFieldId, String?) -> Result<Map<String, String>>,
    private val answerApi: suspend (ApiRequest) -> ApiResponse,
    private val scope: CoroutineScope,
    private val onRendered: () -> Unit = {},
    private val onScriptError: (String) -> Unit = {},
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
            "rendered" -> if (isMainFrame) onRendered()
            "setValue" -> if (isMainFrame) setValue(message, replyProxy)
            "scriptError" -> onScriptError(message.string("message").orEmpty())
            "api" -> answerApiCall(answerApi, scope, message, replyProxy)
        }
    }

    /** Values changed outside the form (sync, reopen): sends the form again with the new values. */
    fun reload() {
        page?.let(::sendForm)
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
                    Timber.e(error, "Could not load the custom form of %s", instance.dataSetUid)
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
            Timber.w("Custom form write to %s refused: not a data element of %s", dataElement, instance.dataSetUid)
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
                put("dataSet", instance.dataSetUid)
                put("period", instance.periodId)
                put("orgUnit", instance.organisationUnitUid)
                put("attributeOptionCombo", instance.attributeOptionComboUid)
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
