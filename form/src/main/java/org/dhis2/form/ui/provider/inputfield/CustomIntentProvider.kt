package org.dhis2.form.ui.provider.inputfield

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.mobile.commons.model.CustomIntentResponseDataModel
import org.dhis2.mobile.commons.model.CustomIntentResponseExtraType
import org.hisp.dhis.mobile.ui.designsystem.component.CustomIntentState
import org.hisp.dhis.mobile.ui.designsystem.component.InputCustomIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import timber.log.Timber

@Composable
fun ProvideCustomIntentInput(
    fieldUiModel: FieldUiModel,
    resources: ResourceManager,
    intentHandler: (FormIntent) -> Unit,
) {
    var values =
        remember(fieldUiModel) {
            fieldUiModel.value?.takeIf { it.isNotEmpty() }?.let { value ->
                mutableStateListOf(*value.split(",").toTypedArray())
            } ?: mutableStateListOf()
        }
    var customIntentState by remember(values) {
        mutableStateOf(getCustomIntentState(values))
    }
    val errorGettingDataMessage =
        SupportingTextData(
            state = SupportingTextState.ERROR,
            text = resources.getString(R.string.custom_intent_error),
        )
    val supportingTextList = remember { fieldUiModel.supportingText()?.toMutableList() ?: mutableListOf() }
    var inputShellState by remember { mutableStateOf(fieldUiModel.inputState()) }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val recoveredData =
                    fieldUiModel.customIntent?.customIntentResponse?.let {
                        mapIntentResponseData(
                            it,
                            result,
                        )
                    }
                if (recoveredData.isNullOrEmpty()) {
                    values = mutableStateListOf()
                    inputShellState = InputShellState.ERROR
                } else {
                    values = mutableStateListOf(*recoveredData.toTypedArray())
                    intentHandler(
                        FormIntent.OnSave(
                            fieldUiModel.uid,
                            recoveredData.joinToString(separator = ","),
                            fieldUiModel.valueType,
                        ),
                    )
                }
                customIntentState = getCustomIntentState(values)
            } else {
                customIntentState = CustomIntentState.LAUNCH
                inputShellState = InputShellState.ERROR
                if (!supportingTextList.contains(errorGettingDataMessage)) {
                    supportingTextList.add(
                        errorGettingDataMessage,
                    )
                }
            }
        }
    InputCustomIntent(
        title = fieldUiModel.label,
        buttonText = resources.getString(R.string.custom_intent_launch),
        supportingText = supportingTextList.toList(),
        inputShellState = inputShellState,
        onLaunch = {
            customIntentState = CustomIntentState.LOADING
            if (supportingTextList.contains(errorGettingDataMessage)) {
                supportingTextList.remove(errorGettingDataMessage)
            }
            val intentData = fieldUiModel.customIntent?.let { mapIntentData(it) }
            val intent =
                Intent.createChooser(
                    intentData,
                    fieldUiModel.customIntent?.name ?: resources.getString(R.string.select_app_intent),
                )
            launcher.launch(intent)
        },
        onClear = {
            values.clear()
            intentHandler(FormIntent.ClearValue(fieldUiModel.uid))
        },
        customIntentState = customIntentState,
        values = values.toList(),
    )
}

fun mapIntentData(customIntent: CustomIntentModel): Intent =
    Intent(customIntent.packageName).apply {
        customIntent.customIntentRequest.forEach { argument ->
            when (val value = argument.value) {
                is String -> putExtra(argument.key, value)
                is Int -> putExtra(argument.key, value)
                is Double -> putExtra(argument.key, value)
                is Long -> putExtra(argument.key, value)
                is Short -> putExtra(argument.key, value)
                is Boolean -> putExtra(argument.key, value)
                else -> putExtra(argument.key, value.toString())
            }
        }
    }

fun mapIntentResponseData(
    customIntentResponse: List<CustomIntentResponseDataModel>,
    result: ActivityResult,
): List<String>? {
    val responseData = mutableListOf<String>()
    val intent = result.data ?: return null

    val objectCache = mapOf<String, JsonObject?>()
    val listCache = mapOf<String, List<JsonObject>?>()

    customIntentResponse.forEach { extra ->
        if (!intent.hasExtra(extra.name)) return@forEach

        extractValue(extra, intent, objectCache, listCache)?.let {
            responseData.addAll(it)
        }
    }

    return responseData.ifEmpty { null }
}

private fun extractValue(
    extra: CustomIntentResponseDataModel,
    intent: Intent,
    objectCache: Map<String, JsonObject?>,
    listCache: Map<String, List<JsonObject>?>,
): List<String>? =
    when (extra.extraType) {
        CustomIntentResponseExtraType.STRING ->
            intent.getStringExtra(extra.name)?.let { listOf(it) }

        CustomIntentResponseExtraType.INTEGER ->
            listOf(intent.getIntExtra(extra.name, 0).toString())

        CustomIntentResponseExtraType.BOOLEAN ->
            listOf(intent.getBooleanExtra(extra.name, false).toString())

        CustomIntentResponseExtraType.FLOAT ->
            listOf(intent.getFloatExtra(extra.name, 0f).toString())

        CustomIntentResponseExtraType.OBJECT ->
            extractObjectValue(extra, intent, objectCache)

        CustomIntentResponseExtraType.LIST_OF_OBJECTS ->
            extractListValues(extra, intent, listCache)
    }

private fun extractObjectValue(
    extra: CustomIntentResponseDataModel,
    intent: Intent,
    objectCache: Map<String, JsonObject?>,
): List<String>? {
    val jsonString = intent.getStringExtra(extra.name) ?: return null
    val jsonObject = objectCache[jsonString] ?: getComplexObject(jsonString) ?: return null

    return if (jsonObject.has(extra.key)) {
        listOf(jsonObject[extra.key].asString)
    } else {
        null
    }
}

private fun extractListValues(
    extra: CustomIntentResponseDataModel,
    intent: Intent,
    listCache: Map<String, List<JsonObject>?>,
): List<String>? {
    val jsonString = intent.getStringExtra(extra.name) ?: return null
    val objectsList = listCache[jsonString] ?: getListOfObjects(jsonString) ?: return null

    return objectsList
        .filter { it.has(extra.key) }
        .map { it[extra.key].asString }
        .takeIf { it.isNotEmpty() }
}

fun getComplexObject(jsonString: String): JsonObject? =
    try {
        val gson = Gson()
        gson.fromJson(jsonString, JsonObject::class.java)
    } catch (e: Exception) {
        Timber.d(e.message)
        null
    }

fun getListOfObjects(jsonString: String): List<JsonObject>? =
    try {
        val gson = Gson()
        val listType =
            com.google.gson.reflect.TypeToken
                .getParameterized(List::class.java, JsonObject::class.java)
                .type
        gson.fromJson(jsonString, listType)
    } catch (e: Exception) {
        Timber.d(e.message)
        null
    }

fun getCustomIntentState(values: SnapshotStateList<String>): CustomIntentState =
    if (values.isEmpty()) {
        CustomIntentState.LAUNCH
    } else {
        CustomIntentState.LOADED
    }
