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
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.mobile.commons.model.CustomIntentResponseDataModel
import org.dhis2.mobile.commons.model.CustomIntentResponseExtraType
import org.hisp.dhis.mobile.ui.designsystem.component.CustomIntentState
import org.hisp.dhis.mobile.ui.designsystem.component.InputCustomIntent

@Composable
fun ProvideCustomIntentInput(
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var values = remember(fieldUiModel) {
        fieldUiModel.value?.takeIf { it.isNotEmpty() }?.let { value ->
            mutableStateListOf(*value.split(",").toTypedArray())
        } ?: mutableStateListOf()
    }
    var state by remember(values) {
        mutableStateOf(getCustomIntentState(values))
    }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val recoveredData = fieldUiModel.customIntent?.customIntentResponse?.let {
                    mapIntentResponseData(
                        it,
                        result,
                    )
                }
                values = if (recoveredData.isNullOrEmpty()) {
                    mutableStateListOf()
                } else {
                    mutableStateListOf(*recoveredData.toTypedArray())
                }
                intentHandler(
                    FormIntent.OnSave(
                        fieldUiModel.uid,
                        recoveredData?.joinToString(separator = ","),
                        fieldUiModel.valueType,
                    ),
                )
                state = getCustomIntentState(values)
            } else {
                state = CustomIntentState.LAUNCH
            }
        }
    //TODO remove hardcoded strings
    InputCustomIntent(
        title = fieldUiModel.label,
        buttonText = "launch",
        onLaunch = {
            state = CustomIntentState.LOADING
            val intentData = fieldUiModel.customIntent?.let { mapIntentData(it) }
            val intent = Intent.createChooser(
                intentData,
                fieldUiModel.customIntent?.name ?: "Select an app to launch intent",
            )
            launcher.launch(intent)
        },
        onClear = {
            values.clear()
            intentHandler(FormIntent.ClearValue(fieldUiModel.uid))
        },
        customIntentState = state,
        values = values.toList(),
    )
}

fun mapIntentData(customIntent: CustomIntentModel): Intent {
    return Intent(customIntent.packageName).apply {
        customIntent.customIntentRequest.forEach { argument ->
            putExtra(argument.key, argument.value)
        }
        // TODO : Remove this hardcoded value when the versionName is not needed
        putExtra("versionCode", 202501020)
    }
}

fun mapIntentResponseData(customIntentResponse: List<CustomIntentResponseDataModel>, result: ActivityResult): List<String>? {
    val responseData = mutableListOf<String>()
    customIntentResponse.forEach { extra ->
        when (extra.extraType) {
            CustomIntentResponseExtraType.STRING -> {
                result.data?.getStringExtra(extra.name)?.let { responseData.add(it) }
            }
            CustomIntentResponseExtraType.INTEGER -> {
                result.data?.getIntExtra(extra.name, 0)?.let { responseData.add(it.toString()) }
            }
            CustomIntentResponseExtraType.BOOLEAN -> {
                result.data?.getBooleanExtra(extra.name, false)?.let { responseData.add(it.toString()) }
            }
            CustomIntentResponseExtraType.FLOAT -> {
                result.data?.getFloatExtra(extra.name, 0f)?.let { responseData.add(it.toString()) }
            }
            CustomIntentResponseExtraType.OBJECT -> {
                result.data?.getStringExtra(extra.name)?.let { jsonString ->
                    val complexObject = getComplexObject(jsonString)
                    complexObject?.let { jsonObject ->
                        extra.keys?.forEach { key ->
                            if (jsonObject.has(key)) {
                                val value = jsonObject.get(key).asString
                                responseData.add(value)
                            }
                        }
                    }
                }
            }
            CustomIntentResponseExtraType.LIST_OF_OBJECTS -> {
                result.data?.getStringExtra(extra.name)?.let { jsonString ->
                    val objectsList = getListOfObjects(jsonString)
                    objectsList?.forEach { jsonObject ->
                        extra.keys?.forEach { key ->
                            if (jsonObject.has(key)) {
                                val value = jsonObject.get(key).asString
                                responseData.add(value)
                            }
                        }
                    }
                }
            }
        }
    }

    return responseData.ifEmpty { null }
}

fun getComplexObject(jsonString: String): JsonObject? {
    return try {
        val gson = Gson()
        gson.fromJson(jsonString, JsonObject::class.java)
    } catch (e: Exception) {
        null // Handle parsing error
    }
}

fun getListOfObjects(jsonString: String): List<JsonObject>? {
    return try {
        val gson = Gson()
        val listType = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, JsonObject::class.java).type
        gson.fromJson(jsonString, listType)
    } catch (e: Exception) {
        null // Handle parsing error
    }
}

fun getCustomIntentState(values: SnapshotStateList<String>): CustomIntentState {
    return if (values.isEmpty()) {
        CustomIntentState.LAUNCH
    } else {
        CustomIntentState.LOADED
    }
}
