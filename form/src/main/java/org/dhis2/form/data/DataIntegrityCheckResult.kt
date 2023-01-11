package org.dhis2.form.data

import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue

sealed class DataIntegrityCheckResult(
    open val canComplete: Boolean = false,
    open val onCompleteMessage: String? = null,
    open val allowDiscard: Boolean = false
)

data class MissingMandatoryResult(
    val mandatoryFields: Map<String, String>,
    val errorFields: List<FieldWithIssue>,
    val warningFields: List<FieldWithIssue>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val allowDiscard: Boolean
) : DataIntegrityCheckResult(canComplete, onCompleteMessage, allowDiscard)

data class FieldsWithErrorResult(
    val mandatoryFields: Map<String, String>,
    val fieldUidErrorList: List<FieldWithIssue>,
    val warningFields: List<FieldWithIssue>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val allowDiscard: Boolean
) : DataIntegrityCheckResult(canComplete, onCompleteMessage, allowDiscard)

data class FieldsWithWarningResult(
    val fieldUidWarningList: List<FieldWithIssue>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?
) : DataIntegrityCheckResult(canComplete, onCompleteMessage)

data class SuccessfulResult(
    val extraData: String? = null,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?
) : DataIntegrityCheckResult(canComplete, onCompleteMessage)

object NotSavedResult : DataIntegrityCheckResult()
