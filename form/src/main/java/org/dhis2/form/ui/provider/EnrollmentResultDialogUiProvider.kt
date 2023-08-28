package org.dhis2.form.ui.provider

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.data.DataIntegrityCheckResult
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FieldsWithWarningResult
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.NotSavedResult
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.ui.dialogs.bottomsheet.IssueType

class EnrollmentResultDialogUiProvider(val resourceManager: ResourceManager) {

    fun provideDataEntryUiModel(
        result: DataIntegrityCheckResult,
    ): Pair<BottomSheetDialogUiModel, List<FieldWithIssue>>? {
        with(resourceManager) {
            return when (result) {
                is FieldsWithErrorResult -> {
                    val model = BottomSheetDialogUiModel(
                        title = getString(R.string.not_saved),
                        message = getErrorSubtitle(result.allowDiscard),
                        iconResource = R.drawable.ic_error_outline,
                        mainButton = DialogButtonStyle.MainButton(R.string.review),
                        secondaryButton = when {
                            result.allowDiscard -> DialogButtonStyle.DiscardButton()
                            else -> null
                        },
                    )
                    val fieldsWithIssues = getFieldsWithIssues(
                        result.fieldUidErrorList,
                        result.mandatoryFields.keys.toList(),
                        result.warningFields,
                    )
                    Pair(model, fieldsWithIssues)
                }
                is FieldsWithWarningResult -> {
                    val model = BottomSheetDialogUiModel(
                        title = getString(R.string.saved),
                        message = getString(R.string.review_message),
                        iconResource = R.drawable.ic_warning_alert,
                        mainButton = DialogButtonStyle.MainButton(R.string.review),
                        secondaryButton = DialogButtonStyle.SecondaryButton(R.string.not_now),
                    )
                    val fieldsWithIssues = result.fieldUidWarningList

                    Pair(model, fieldsWithIssues)
                }
                is MissingMandatoryResult -> {
                    val model = BottomSheetDialogUiModel(
                        title = getString(R.string.not_saved),
                        message = getMandatorySubtitle(result.allowDiscard),
                        iconResource = R.drawable.ic_error_outline,

                        mainButton = DialogButtonStyle.MainButton(
                            when {
                                result.allowDiscard -> R.string.keep_editing
                                else -> R.string.review
                            },
                        ),
                        secondaryButton = when {
                            result.allowDiscard -> DialogButtonStyle.DiscardButton()
                            else -> null
                        },
                    )
                    val fieldsWithIssues = getFieldsWithIssues(
                        mandatoryFields = result.mandatoryFields.keys.toList(),
                        warningFields = result.warningFields,
                    )
                    Pair(model, fieldsWithIssues)
                }
                is NotSavedResult -> {
                    val model = BottomSheetDialogUiModel(
                        title = getString(R.string.not_saved),
                        message = getString(R.string.discard_go_back),
                        iconResource = R.drawable.ic_warning_alert,
                        mainButton = DialogButtonStyle.MainButton(R.string.keep_editing),
                        secondaryButton = DialogButtonStyle.DiscardButton(),
                    )
                    Pair(model, emptyList())
                }
                else -> null
            }
        }
    }

    private fun getFieldsWithIssues(
        errorFields: List<FieldWithIssue> = emptyList(),
        mandatoryFields: List<String> = emptyList(),
        warningFields: List<FieldWithIssue> = emptyList(),
    ): List<FieldWithIssue> {
        return errorFields.plus(
            mandatoryFields.map {
                FieldWithIssue(
                    "uid",
                    it,
                    IssueType.MANDATORY,
                    resourceManager.getString(R.string.mandatory_field),
                )
            },
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
