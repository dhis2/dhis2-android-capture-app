package org.dhis2.form.data

sealed class DataIntegrityCheckResult(
    open val canComplete: Boolean = false,
    open val onCompleteMessage: String? = null,
    open val allowDiscard: Boolean = false
)

data class MissingMandatoryResult(
    val mandatoryFields: Map<String, String>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val allowDiscard: Boolean
) : DataIntegrityCheckResult(canComplete, onCompleteMessage, allowDiscard)

data class FieldsWithErrorResult(
    val fieldUidErrorList: List<String>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val allowDiscard: Boolean
) : DataIntegrityCheckResult(canComplete, onCompleteMessage, allowDiscard)

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

object NotSavedResult : DataIntegrityCheckResult()
