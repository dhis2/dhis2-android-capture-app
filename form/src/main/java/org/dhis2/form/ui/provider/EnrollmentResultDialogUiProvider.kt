package org.dhis2.form.ui.provider

import org.dhis2.commons.data.FieldWithIssue
import org.dhis2.commons.data.IssueType
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.data.DataIntegrityCheckResult
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FieldsWithWarningResult
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.NotSavedResult
import org.dhis2.form.ui.dialog.DataEntryDialogUiModel
import org.dhis2.form.ui.dialog.DialogButtonStyle

class EnrollmentResultDialogUiProvider(val resourceManager: ResourceManager) {

    fun provideDataEntryUiModel(result: DataIntegrityCheckResult): DataEntryDialogUiModel? {
        with(resourceManager) {
            return when (result) {
                is FieldsWithErrorResult -> DataEntryDialogUiModel(
                    title = getString(R.string.not_saved),
                    subtitle = getErrorSubtitle(result.allowDiscard),
                    iconResource = R.drawable.ic_error_outline,
                    fieldsWithIssues = getFieldsWithIssues(
                        result.fieldUidErrorList,
                        result.mandatoryFields.keys.toList(),
                        result.warningFields
                    ),
                    mainButton = DialogButtonStyle.MainButton(R.string.review),
                    secondaryButton = when {
                        result.allowDiscard -> DialogButtonStyle.DiscardButton()
                        else -> null
                    }
                )
                is FieldsWithWarningResult -> DataEntryDialogUiModel(
                    title = getString(R.string.saved),
                    subtitle = getString(R.string.review_message),
                    iconResource = R.drawable.ic_alert,
                    fieldsWithIssues = result.fieldUidWarningList,
                    mainButton = DialogButtonStyle.MainButton(R.string.review),
                    secondaryButton = DialogButtonStyle.SecondaryButton(R.string.not_now)
                )
                is MissingMandatoryResult -> DataEntryDialogUiModel(
                    title = getString(R.string.not_saved),
                    subtitle = getMandatorySubtitle(result.allowDiscard),
                    iconResource = R.drawable.ic_error_outline,
                    fieldsWithIssues = getFieldsWithIssues(
                        mandatoryFields = result.mandatoryFields.keys.toList(),
                        warningFields = result.warningFields
                    ),
                    mainButton = DialogButtonStyle.MainButton(
                        when {
                            result.allowDiscard -> R.string.keep_editing
                            else -> R.string.review
                        }
                    ),
                    secondaryButton = when {
                        result.allowDiscard -> DialogButtonStyle.DiscardButton()
                        else -> null
                    }
                )
                NotSavedResult -> DataEntryDialogUiModel(
                    title = getString(R.string.not_saved),
                    subtitle = getString(R.string.discard_go_back),
                    iconResource = R.drawable.ic_alert,
                    mainButton = DialogButtonStyle.MainButton(R.string.keep_editing),
                    secondaryButton = DialogButtonStyle.DiscardButton()
                )
                else -> null
            }
        }
    }

    private fun getFieldsWithIssues(
        errorFields: List<FieldWithIssue> = emptyList(),
        mandatoryFields: List<String> = emptyList(),
        warningFields: List<FieldWithIssue> = emptyList()
    ): List<FieldWithIssue> {
        return errorFields.plus(
            mandatoryFields.map {
                FieldWithIssue(
                    "uid",
                    it,
                    IssueType.MANDATORY,
                    resourceManager.getString(R.string.mandatory_field)
                )
            }
        ).plus(warningFields)
    }

    private fun ResourceManager.getErrorSubtitle(allowDiscard: Boolean) = when {
        allowDiscard -> getString(R.string.field_errors_not_saved_discard)
        else -> getString(R.string.field_errors_not_saved)
    }

    private fun ResourceManager.getMandatorySubtitle(allowDiscard: Boolean) = when {
        allowDiscard -> getString(R.string.fields_mandatory_missing_discard)
        else -> getString(R.string.fields_mandatory_missing)
    }
}
