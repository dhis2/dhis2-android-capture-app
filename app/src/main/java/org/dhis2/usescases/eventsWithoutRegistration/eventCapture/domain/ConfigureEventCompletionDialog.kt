package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain

import org.dhis2.commons.data.FieldWithIssue
import org.dhis2.commons.data.IssueType
import org.dhis2.form.data.ItemWithWarning
import org.dhis2.ui.DataEntryDialogUiModel
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
        fieldUidErrorList: MutableList<String>,
        emptyMandatoryFields: MutableMap<String, String>,
        fieldsWithWarning: MutableList<ItemWithWarning>
    ): EventCompletionDialog {
        val icon: Int
        val title: String
        val subtitle: String
        val mainButtonAction: EventCompletionButtons
        val secondaryButtonAction: EventCompletionButtons

        if (fieldUidErrorList.isNotEmpty()) { //Error
            icon = provider.provideRedAlertIcon()
            title = provider.provideNotSavedText()
            subtitle = provider.provideErrorInfo()
            mainButtonAction =
                EventCompletionButtons(
                    provider.provideReview(),
                    FormBottomDialog.ActionType.CHECK_FIELDS
                )
            secondaryButtonAction =
                EventCompletionButtons(provider.provideNotNow(), FormBottomDialog.ActionType.FINISH)
        } else if (emptyMandatoryFields.isNotEmpty()) { //mandatory
            icon = provider.provideSavedIcon()
            title = provider.provideSavedText()
            subtitle = provider.provideMandatoryInfo()
            mainButtonAction =
                EventCompletionButtons(
                    provider.provideReview(),
                    FormBottomDialog.ActionType.CHECK_FIELDS
                )
            secondaryButtonAction =
                EventCompletionButtons(provider.provideNotNow(), FormBottomDialog.ActionType.FINISH)
        } else { //warning
            icon = provider.provideYellowAlertIcon()
            title = provider.provideSavedText()
            subtitle = provider.provideWarningInfo()
            mainButtonAction =
                EventCompletionButtons(
                    provider.provideComplete(),
                    FormBottomDialog.ActionType.COMPLETE
                )
            secondaryButtonAction =
                EventCompletionButtons(provider.provideNotNow(), FormBottomDialog.ActionType.FINISH)
        }

        val fieldsWithIssues: MutableList<FieldWithIssue> = ArrayList()
        for (field in fieldUidErrorList) {
            val fieldWithIssue = FieldWithIssue(
                field,
                IssueType.ERROR,
                provider.provideErrorField()
            )
            fieldsWithIssues.add(fieldWithIssue)
        }

        for (field in emptyMandatoryFields.keys) {
            val fieldWithIssue = FieldWithIssue(
                field,
                IssueType.MANDATORY,
                provider.provideMandatoryField()
            )
            fieldsWithIssues.add(fieldWithIssue)
        }

        fieldsWithWarning.forEach {
            val fieldWithIssue = FieldWithIssue(
                it.label ?: "",
                IssueType.WARNING,
                it.message
            )
            fieldsWithIssues.add(fieldWithIssue)
        }

        val dataEntryDialogUiModel = DataEntryDialogUiModel(
            title,
            subtitle,
            icon,
            fieldsWithIssues,
            MainButton(mainButtonAction.text),
            SecondaryButton(secondaryButtonAction.text)
        )

        return EventCompletionDialog(
            dataEntryDialogUiModel = dataEntryDialogUiModel,
            mainButton = mainButtonAction,
            secondaryButton = secondaryButtonAction
        )
    }
}
