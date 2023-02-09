package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain

import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle.CompleteButton
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle.MainButton
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle.SecondaryButton
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.ui.dialogs.bottomsheet.IssueType
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog.DialogType.COMPLETE_ERROR
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog.DialogType.ERROR
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog.DialogType.MANDATORY
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog.DialogType.SUCCESSFUL
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog.DialogType.WARNING
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
        warningFields: List<FieldWithIssue>,
        canComplete: Boolean,
        onCompleteMessage: String?
    ): EventCompletionDialog {
        val dialogType = getDialogType(
            errorFields,
            mandatoryFields,
            warningFields,
            !canComplete && onCompleteMessage != null
        )
        val mainButton = getMainButton(dialogType)
        val secondaryButton = EventCompletionButtons(
            SecondaryButton(provider.provideNotNow()),
            FormBottomDialog.ActionType.FINISH
        )
        val bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = getTitle(dialogType),
            message = getSubtitle(dialogType),
            iconResource = getIcon(dialogType),
            mainButton = mainButton.buttonStyle,
            secondaryButton = secondaryButton.buttonStyle
        )

        return EventCompletionDialog(
            bottomSheetDialogUiModel = bottomSheetDialogUiModel,
            mainButtonAction = mainButton.action,
            secondaryButtonAction = secondaryButton.action,
            fieldsWithIssues = getFieldsWithIssues(
                errorFields = errorFields,
                mandatoryFields = mandatoryFields.keys.toList(),
                warningFields = warningFields,
                onCompleteField = getOnCompleteMessage(canComplete, onCompleteMessage)
            )
        )
    }

    private fun getTitle(type: DialogType) = when (type) {
        ERROR -> provider.provideNotSavedText()
        else -> provider.provideSavedText()
    }

    private fun getSubtitle(type: DialogType) = when (type) {
        ERROR -> provider.provideErrorInfo()
        MANDATORY -> provider.provideMandatoryInfo()
        WARNING -> provider.provideWarningInfo()
        SUCCESSFUL -> provider.provideCompleteInfo()
        COMPLETE_ERROR -> provider.provideOnCompleteErrorInfo()
    }

    private fun getIcon(type: DialogType) = when (type) {
        ERROR, COMPLETE_ERROR -> provider.provideRedAlertIcon()
        MANDATORY -> provider.provideSavedIcon()
        WARNING -> provider.provideYellowAlertIcon()
        SUCCESSFUL -> provider.provideSavedIcon()
    }

    private fun getMainButton(type: DialogType) = when (type) {
        ERROR,
        MANDATORY,
        COMPLETE_ERROR -> EventCompletionButtons(
            MainButton(provider.provideReview()),
            FormBottomDialog.ActionType.CHECK_FIELDS
        )
        WARNING,
        SUCCESSFUL -> EventCompletionButtons(
            CompleteButton(),
            FormBottomDialog.ActionType.COMPLETE
        )
    }

    private fun getFieldsWithIssues(
        errorFields: List<FieldWithIssue>,
        mandatoryFields: List<String>,
        warningFields: List<FieldWithIssue>,
        onCompleteField: List<FieldWithIssue>
    ): List<FieldWithIssue> {
        return onCompleteField
            .plus(errorFields)
            .plus(
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

    private fun getOnCompleteMessage(
        canComplete: Boolean,
        onCompleteMessage: String?
    ): List<FieldWithIssue> {
        val issueOnComplete = onCompleteMessage?.let {
            FieldWithIssue(
                fieldUid = "",
                fieldName = it,
                issueType = when (canComplete) {
                    false -> IssueType.ERROR_ON_COMPLETE
                    else -> IssueType.WARNING_ON_COMPLETE
                },
                message = ""
            )
        }
        return issueOnComplete?.let { listOf(it) } ?: emptyList()
    }

    private fun getDialogType(
        errorFields: List<FieldWithIssue>,
        mandatoryFields: Map<String, String>,
        warningFields: List<FieldWithIssue>,
        errorOnComplete: Boolean
    ) = when {
        errorOnComplete -> {
            COMPLETE_ERROR
        }
        errorFields.isNotEmpty() -> {
            ERROR
        }
        mandatoryFields.isNotEmpty() -> {
            MANDATORY
        }
        warningFields.isNotEmpty() -> {
            WARNING
        }
        else -> {
            SUCCESSFUL
        }
    }

    private enum class DialogType { ERROR, MANDATORY, WARNING, SUCCESSFUL, COMPLETE_ERROR }
}
