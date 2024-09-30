package org.dhis2.form.data

import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.hisp.dhis.android.core.common.ValidationStrategy

sealed class DataIntegrityCheckResult(
    open val canComplete: Boolean = false,
    open val onCompleteMessage: String? = null,
    open val allowDiscard: Boolean = false,
    open val validationStrategy: ValidationStrategy? = null,
)

data class MissingMandatoryResult(
    val mandatoryFields: Map<String, String>,
    val errorFields: List<FieldWithIssue>,
    val warningFields: List<FieldWithIssue>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val allowDiscard: Boolean,
    override val validationStrategy: ValidationStrategy?,

) : DataIntegrityCheckResult(canComplete, onCompleteMessage, allowDiscard, validationStrategy = validationStrategy)

data class FieldsWithErrorResult(
    val mandatoryFields: Map<String, String>,
    val fieldUidErrorList: List<FieldWithIssue>,
    val warningFields: List<FieldWithIssue>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val allowDiscard: Boolean,
    override val validationStrategy: ValidationStrategy?,
) : DataIntegrityCheckResult(canComplete, onCompleteMessage, allowDiscard, validationStrategy = validationStrategy)

data class FieldsWithWarningResult(
    val fieldUidWarningList: List<FieldWithIssue>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val validationStrategy: ValidationStrategy?,
) : DataIntegrityCheckResult(canComplete, onCompleteMessage, validationStrategy = validationStrategy)

data class SuccessfulResult(
    val extraData: String? = null,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val validationStrategy: ValidationStrategy?,
) : DataIntegrityCheckResult(canComplete, onCompleteMessage, validationStrategy = validationStrategy)

object NotSavedResult : DataIntegrityCheckResult()
