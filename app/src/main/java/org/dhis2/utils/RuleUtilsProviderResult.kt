package org.dhis2.utils

data class RuleUtilsProviderResult(
    val canComplete: Boolean,
    val messageOnComplete: String?,
    val fieldsWithErrors: List<FieldWithError>,
    val unsupportedRules: List<String>,
    val fieldsToUpdate: List<String>
) {
    fun errorMap(): Map<String, String> = fieldsWithErrors.map {
        it.fieldUid to it.errorMessage
    }.toMap()
}

data class FieldWithError(val fieldUid: String, val errorMessage: String)
