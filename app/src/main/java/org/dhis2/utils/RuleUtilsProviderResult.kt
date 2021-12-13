package org.dhis2.utils

import android.content.Context
import androidx.annotation.StringRes
import kotlin.text.Typography.bullet
import org.dhis2.R

data class RuleUtilsProviderResult(
    val canComplete: Boolean,
    val messageOnComplete: String?,
    val fieldsWithErrors: List<FieldWithError>,
    val unsupportedRules: List<String>,
    val fieldsToUpdate: List<String>,
    val stagesToHide: List<String>,
    val configurationErrors: List<RulesUtilsProviderConfigurationError>
) {
    fun errorMap(): Map<String, String> = fieldsWithErrors.map {
        it.fieldUid to it.errorMessage
    }.toMap()
}

data class FieldWithError(val fieldUid: String, val errorMessage: String)

data class RulesUtilsProviderConfigurationError(
    val programRuleUid: String?,
    val actionType: ActionType = ActionType.NONE,
    val error: ConfigurationError,
    val extraData: List<String>
)

enum class ActionType {
    ASSIGN, NONE
}

enum class ConfigurationError(@StringRes val message: Int) {
    VALUE_TO_ASSIGN_NOT_IN_OPTION_SET(R.string.conf_error_value_to_assign_option_set),
    CURRENT_VALUE_NOT_IN_OPTION_SET(R.string.conf_error_value_option_set)
}

fun List<RulesUtilsProviderConfigurationError>.toMessage(context: Context): String {
    return joinToString(separator = "\n\n $bullet", prefix = "$bullet") { configurationError ->
        context.getString(configurationError.error.message)
            .format(*configurationError.extraData.toTypedArray())
    }
}
