package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain

import org.dhis2.commons.data.FieldWithIssue
import org.dhis2.commons.data.IssueType
import org.dhis2.ui.DataEntryDialogUiModel
import org.dhis2.ui.DialogButtonStyle.CompleteButton
import org.dhis2.ui.DialogButtonStyle.MainButton
import org.dhis2.ui.DialogButtonStyle.SecondaryButton
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCompletionButtons
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCompletionDialog
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.provider.EventCaptureResourcesProvider
import org.dhis2.utils.customviews.FormBottomDialog

class ConfigureEventCompletionDialog(
    val provider: EventCaptureResourcesProvider
) {

    operator fun invoke(
        errorFields: List<FieldWithIssue>,
        mandatoryFields: Map<String, String>,
        warningFields: MutableList<FieldWithIssue>
    ): EventCompletionDialog {
        val icon: Int
        val subtitle: String
        val mainButtonAction: EventCompletionButtons
        val secondaryButtonAction: EventCompletionButtons

        if (errorFields.isNotEmpty()) { // Error
            icon = provider.provideRedAlertIcon()
            subtitle = provider.provideErrorInfo()
            mainButtonAction = EventCompletionButtons(
                MainButton(provider.provideReview()),
                FormBottomDialog.ActionType.CHECK_FIELDS
            )
            secondaryButtonAction = EventCompletionButtons(
                SecondaryButton(provider.provideNotNow()),
                FormBottomDialog.ActionType.FINISH
            )
        } else if (mandatoryFields.isNotEmpty()) { // mandatory
            icon = provider.provideSavedIcon()
            subtitle = provider.provideMandatoryInfo()
            mainButtonAction = EventCompletionButtons(
                MainButton(provider.provideReview()),
                FormBottomDialog.ActionType.CHECK_FIELDS
            )
            secondaryButtonAction = EventCompletionButtons(
                SecondaryButton(provider.provideNotNow()),
                FormBottomDialog.ActionType.FINISH
            )
        } else if (warningFields.isNotEmpty()) { // warning
            icon = provider.provideYellowAlertIcon()
            subtitle = provider.provideWarningInfo()
            mainButtonAction = EventCompletionButtons(
                CompleteButton,
                FormBottomDialog.ActionType.COMPLETE
            )
            secondaryButtonAction = EventCompletionButtons(
                SecondaryButton(provider.provideNotNow()),
                FormBottomDialog.ActionType.FINISH
            )
        } else { // Successful
            icon = provider.provideSavedIcon()
            subtitle = provider.provideCompleteInfo()
            mainButtonAction = EventCompletionButtons(
                CompleteButton,
                FormBottomDialog.ActionType.COMPLETE
            )
            secondaryButtonAction = EventCompletionButtons(
                SecondaryButton(provider.provideNotNow()),
                FormBottomDialog.ActionType.FINISH
            )
        }

        val dataEntryDialogUiModel = DataEntryDialogUiModel(
            title = getTitle(errorFields.isNotEmpty()),
            subtitle = subtitle,
            iconResource = icon,
            fieldsWithIssues = getFieldsWithIssues(
                errorFields = errorFields,
                mandatoryFields = mandatoryFields.keys.toList(),
                warningFields = warningFields
            ),
            mainButton = mainButtonAction.buttonStyle,
            secondaryButton = secondaryButtonAction.buttonStyle
        )

        return EventCompletionDialog(
            dataEntryDialogUiModel = dataEntryDialogUiModel,
            mainButtonAction = mainButtonAction.action,
            secondaryButtonAction = secondaryButtonAction.action
        )
    }

    private fun getTitle(areErrors: Boolean): String {
        return if (areErrors) provider.provideNotSavedText() else provider.provideSavedText()
    }

    private fun getFieldsWithIssues(
        errorFields: List<FieldWithIssue>,
        mandatoryFields: List<String>,
        warningFields: List<FieldWithIssue>
    ): List<FieldWithIssue> {
        return errorFields.plus(
            mandatoryFields.map {
                FieldWithIssue(
                    "uid",
                    it,
                    IssueType.MANDATORY,
                    provider.provideMandatoryField()
                )
            }
        ).plus(warningFields)
    }
}
