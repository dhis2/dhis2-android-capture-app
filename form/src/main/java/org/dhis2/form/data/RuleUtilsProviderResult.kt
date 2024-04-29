package org.dhis2.form.data

import android.content.Context
import androidx.annotation.StringRes
import org.dhis2.form.R
import kotlin.text.Typography.bullet

data class RuleUtilsProviderResult(
    val canComplete: Boolean,
    val messageOnComplete: String?,
    val fieldsWithErrors: List<FieldWithError>,
    val fieldsWithWarnings: List<FieldWithError>,
    val unsupportedRules: List<String>,
    val fieldsToUpdate: List<FieldWithNewValue>,
    val stagesToHide: List<String>,
    val configurationErrors: List<RulesUtilsProviderConfigurationError>,
    val optionsToHide: Map<String, List<String>>,
    val optionGroupsToHide: Map<String, List<String>>,
    val optionGroupsToShow: Map<String, List<String>>,
) {
    fun errorMap(): Map<String, String> {
        val map: MutableMap<String, String> = mutableMapOf()
        val iterator = fieldsWithErrors.toMutableList().listIterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            map[item.fieldUid] = item.errorMessage
        }
        return map
    }

    fun warningMap(): Map<String, String> {
        val map: MutableMap<String, String> = mutableMapOf()
        val iterator = fieldsWithWarnings.toMutableList().listIterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            map[item.fieldUid] = item.errorMessage
        }
        return map
    }

    fun optionsToHide(fieldUid: String): List<String> {
        return optionsToHide[fieldUid] ?: mutableListOf()
    }

    fun optionGroupsToHide(fieldUid: String): List<String> {
        return optionGroupsToHide[fieldUid] ?: mutableListOf()
    }

    fun optionGroupsToShow(fieldUid: String): List<String> {
        return optionGroupsToShow[fieldUid] ?: mutableListOf()
    }
}

data class FieldWithNewValue(val fieldUid: String, val newValue: String?)

data class FieldWithError(val fieldUid: String, val errorMessage: String)

data class RulesUtilsProviderConfigurationError(
    val programRuleUid: String?,
    val actionType: ActionType = ActionType.NONE,
    val error: ConfigurationError,
    val extraData: List<String>,
)

enum class ActionType {
    ASSIGN, NONE
}

enum class ConfigurationError(@StringRes val message: Int) {
    VALUE_TO_ASSIGN_NOT_IN_OPTION_SET(R.string.conf_error_value_to_assign_option_set),
    CURRENT_VALUE_NOT_IN_OPTION_SET(R.string.conf_error_value_option_set),
}

fun List<RulesUtilsProviderConfigurationError>.toMessage(context: Context): String {
    return joinToString(separator = "\n\n $bullet", prefix = "$bullet") { configurationError ->
        context.getString(configurationError.error.message)
            .format(*configurationError.extraData.toTypedArray())
    }
}
