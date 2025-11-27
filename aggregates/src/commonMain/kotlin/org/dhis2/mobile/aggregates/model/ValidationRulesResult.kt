package org.dhis2.mobile.aggregates.model

internal data class ValidationRulesResult(
    val validationResultStatus: ValidationResultStatus,
    val violations: List<Violation>,
    val mandatory: Boolean = false,
)

internal data class Violation(
    val description: String?,
    val instruction: String?,
    val dataToReview: List<DataToReview>,
)

internal data class DataToReview(
    val dataElementUid: String,
    val dataElementDisplayName: String?,
    val categoryOptionComboUid: String,
    val categoryOptionComboDisplayName: String?,
    val value: String,
    val isFromDefaultCatCombo: Boolean,
) {
    fun formattedDataLabel(): String =
        if (isFromDefaultCatCombo) {
            dataElementDisplayName ?: dataElementUid
        } else {
            String.format(
                "%s / %s",
                dataElementDisplayName,
                categoryOptionComboDisplayName,
            )
        }
}

internal enum class ValidationResultStatus {
    OK,
    ERROR,
}
