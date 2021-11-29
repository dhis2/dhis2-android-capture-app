package org.dhis2.form.data

sealed class DataIntegrityCheckResult(
    open val canComplete: Boolean,
    open val onCompleteMessage: String?
)

data class MissingMandatoryResult(
    val mandatoryFields: Map<String, String>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?
) : DataIntegrityCheckResult(canComplete, onCompleteMessage)

data class FieldsWithErrorResult(
    val fieldUidErrorList: List<String>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?
) : DataIntegrityCheckResult(canComplete, onCompleteMessage)

data class FieldsWithWarningResult(
    val fieldUidWarningList: List<String>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?
) : DataIntegrityCheckResult(canComplete, onCompleteMessage)

data class SuccessfulResult(
    val extraData: String? = null,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?
) : DataIntegrityCheckResult(canComplete, onCompleteMessage)
