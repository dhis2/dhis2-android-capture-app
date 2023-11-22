package org.dhis2.utils.validationrules

import org.hisp.dhis.android.core.validation.engine.ValidationResult

data class ValidationRuleResult(
    val validationResultStatus: ValidationResult.ValidationResultStatus,
    val violations: List<Violation>,
)

data class Violation(
    val description: String?,
    val instruction: String?,
    val dataToReview: List<DataToReview>,
)

data class DataToReview(
    val dataElementUid: String,
    val dataElementDisplayName: String?,
    val categoryOptionComboUid: String,
    val categoryOptionComboDisplayName: String?,
    val value: String,
    val isFromDefaultCatCombo: Boolean,
) {
    fun formattedDataLabel(): String {
        return if (isFromDefaultCatCombo) {
            dataElementDisplayName ?: dataElementUid
        } else {
            String.format(
                "%s | %s",
                dataElementDisplayName,
                categoryOptionComboDisplayName,
            )
        }
    }
}
