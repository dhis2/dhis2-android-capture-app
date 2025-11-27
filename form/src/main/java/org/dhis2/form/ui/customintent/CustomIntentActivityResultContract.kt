package org.dhis2.form.ui.customintent

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateListOf
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.mobile.commons.model.CustomIntentRequestArgumentModel
import org.dhis2.mobile.commons.model.CustomIntentResponseDataModel
import org.dhis2.mobile.commons.model.CustomIntentResponseExtraType
import timber.log.Timber
import kotlin.collections.forEach

class CustomIntentActivityResultContract : ActivityResultContract<CustomIntentInput, CustomIntentResult>() {
    companion object {
        private var fieldUid: String = ""
        private var customIntent: CustomIntentModel? = null
    }

    override fun createIntent(
        context: Context,
        input: CustomIntentInput,
    ): Intent {
        val intentData =
            mapIntentData(input.customIntent.packageName, input.customIntent.customIntentRequest)
        customIntent = input.customIntent
        fieldUid = input.fieldUid
        return Intent.createChooser(
            intentData,
            input.customIntent.name ?: input.defaultTitle,
        )
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): CustomIntentResult =
        if (resultCode == RESULT_OK) {
            var returnedValues: List<String>?
            val customIntentResponseParsedData =
                customIntent?.let {
                    mapIntentResponseData(it.customIntentResponse, intent)
                }
            if (customIntentResponseParsedData.isNullOrEmpty()) {
                CustomIntentResult.Error(fieldUid = fieldUid)
            } else {
                returnedValues = mutableStateListOf(*customIntentResponseParsedData.toTypedArray())

                CustomIntentResult.Success(
                    fieldUid = fieldUid,
                    value = returnedValues.joinToString(separator = ","),
                )
            }
        } else {
            CustomIntentResult.Error(fieldUid = fieldUid)
        }

    fun mapIntentData(
        packageName: String,
        requestParameters: List<CustomIntentRequestArgumentModel>,
    ): Intent =
        Intent(packageName).apply {
            requestParameters.forEach { argument ->
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
        customIntentResponse: List<CustomIntentResponseDataModel>?,
        intent: Intent?,
    ): List<String>? {
        val responseData = mutableListOf<String>()
        val objectCache = mapOf<String, JsonObject?>()
        val listCache = mapOf<String, List<JsonObject>?>()
        intent?.let { intent } ?: return null
        customIntentResponse?.forEach { extra ->
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
        try {
            val jsonString = intent.getStringExtra(extra.name) ?: return null
            val jsonObject = objectCache[jsonString] ?: getComplexObject(jsonString) ?: return null

            return if (jsonObject.has(extra.key)) {
                listOf(jsonObject[extra.key].asString)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.d(e)
            return null
        }
    }

    private fun extractListValues(
        extra: CustomIntentResponseDataModel,
        intent: Intent,
        listCache: Map<String, List<JsonObject>?>,
    ): List<String>? {
        try {
            val jsonString = intent.getStringExtra(extra.name) ?: return null
            val objectsList = listCache[jsonString] ?: getListOfObjects(jsonString) ?: return null

            return objectsList
                .filter { it.has(extra.key) }
                .map { it[extra.key].asString }
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Timber.d(e)
            return null
        }
    }

    fun getComplexObject(jsonString: String): JsonObject? =
        try {
            val gson = Gson()
            gson.fromJson(jsonString, JsonObject::class.java)
        } catch (e: Exception) {
            Timber.d(e)
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
            Timber.d(e)
            null
        }
}

data class CustomIntentInput(
    val fieldUid: String,
    val customIntent: CustomIntentModel,
    val defaultTitle: String,
)

sealed class CustomIntentResult {
    data class Success(
        val fieldUid: String,
        val value: String,
    ) : CustomIntentResult()

    data class Error(
        val fieldUid: String,
    ) : CustomIntentResult()
}
