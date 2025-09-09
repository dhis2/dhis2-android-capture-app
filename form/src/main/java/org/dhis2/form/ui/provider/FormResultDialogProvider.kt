package org.dhis2.form.ui.provider

import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle.CompleteButton
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle.MainButton
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle.SecondaryButton
import org.dhis2.commons.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.commons.dialogs.bottomsheet.IssueType
import org.dhis2.form.R
import org.dhis2.form.data.DataIntegrityCheckResult
import org.dhis2.form.data.EventRepository
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FieldsWithWarningResult
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.NotSavedResult
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.form.model.EventMode
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.event.EventStatus

class FormResultDialogProvider(
    val provider: FormResultDialogResourcesProvider,
) {
    operator fun invoke(
        canComplete: Boolean,
        onCompleteMessage: String?,
        errorFields: List<FieldWithIssue>,
        emptyMandatoryFields: Map<String, String>,
        warningFields: List<FieldWithIssue>,
        eventMode: EventMode?,
        eventState: EventStatus?,
        result: DataIntegrityCheckResult,
    ): Pair<BottomSheetDialogUiModel, List<FieldWithIssue>> {
        val onCompleteMessages =
            getOnCompleteMessage(
                canComplete,
                onCompleteMessage,
            )
        val dialogType =
            getDialogType(
                errorFields,
                emptyMandatoryFields,
                warningFields,
                onCompleteMessages,
            )
        val showSkipButton =
            when {
                dialogType == DialogType.WARNING || dialogType == DialogType.SUCCESSFUL -> true
                eventState != EventStatus.ACTIVE -> false
                else ->
                    canSkipErrorFix(
                        hasErrorFields = errorFields.isNotEmpty(),
                        hasEmptyMandatoryFields = emptyMandatoryFields.isNotEmpty(),
                        hasEmptyEventCreationMandatoryFields =
                            with(emptyMandatoryFields) {
                                containsValue(EventRepository.EVENT_DETAILS_SECTION_UID) ||
                                    containsValue(EventRepository.EVENT_CATEGORY_COMBO_SECTION_UID)
                            },
                        eventMode = eventMode,
                        validationStrategy = result.eventResultDetails.validationStrategy,
                    )
            }

        val model =
            BottomSheetDialogUiModel(
                title = getTitle(dialogType),
                message = getSubtitle(dialogType, eventState, result.allowDiscard),
                iconResource = getIcon(dialogType),
                mainButton = getMainButton(dialogType, eventState),
                secondaryButton =
                    if (result.allowDiscard) {
                        DialogButtonStyle.DiscardButton()
                    } else if (showSkipButton) {
                        SecondaryButton(R.string.not_now)
                    } else {
                        null
                    },
            )

        return when (result) {
            is FieldsWithErrorResult -> {
                val fieldsWithIssues =
                    getFieldsWithIssues(
                        result.fieldUidErrorList,
                        result.mandatoryFields.keys.toList(),
                        result.warningFields,
                        onCompleteMessages,
                    )
                Pair(model, fieldsWithIssues)
            }
            is FieldsWithWarningResult -> {
                val fieldsWithIssues =
                    getFieldsWithIssues(
                        warningFields = result.fieldUidWarningList,
                        onCompleteFields = onCompleteMessages,
                    )
                return Pair(model, fieldsWithIssues)
            }
            is MissingMandatoryResult -> {
                val fieldsWithIssues =
                    getFieldsWithIssues(
                        mandatoryFields = result.mandatoryFields.keys.toList(),
                        warningFields = result.warningFields,
                    )
                Pair(model, fieldsWithIssues)
            }
            is NotSavedResult -> {
                val notSavedModel =
                    BottomSheetDialogUiModel(
                        title = provider.provideNotSavedText(),
                        message = provider.provideDiscardWarning(),
                        iconResource = R.drawable.ic_warning_alert,
                        mainButton = MainButton(R.string.keep_editing),
                        secondaryButton = DialogButtonStyle.DiscardButton(),
                    )
                Pair(notSavedModel, emptyList())
            }
            is SuccessfulResult -> {
                Pair(model, onCompleteMessages)
            }
        }
    }

    private fun canSkipErrorFix(
        hasErrorFields: Boolean,
        hasEmptyMandatoryFields: Boolean,
        hasEmptyEventCreationMandatoryFields: Boolean,
        eventMode: EventMode?,
        validationStrategy: ValidationStrategy?,
    ): Boolean =
        when (validationStrategy) {
            ValidationStrategy.ON_COMPLETE ->
                when (eventMode) {
                    EventMode.NEW -> !hasEmptyEventCreationMandatoryFields
                    else -> true
                }
            ValidationStrategy.ON_UPDATE_AND_INSERT -> !hasErrorFields && !hasEmptyMandatoryFields
            else -> true
        }

    private fun getTitle(type: DialogType) =
        when (type) {
            DialogType.ERROR -> provider.provideNotSavedText()
            else -> provider.provideSavedText()
        }

    private fun getSubtitle(
        type: DialogType,
        eventState: EventStatus?,
        canDiscard: Boolean,
    ) = when (type) {
        DialogType.ERROR -> if (canDiscard) provider.provideErrorWithDiscard() else provider.provideErrorInfo()
        DialogType.MANDATORY -> provider.provideMandatoryInfo()
        DialogType.WARNING ->
            if (eventState == EventStatus.COMPLETED) {
                provider.provideWarningInfoCompletedEvent()
            } else {
                provider.provideWarningInfo()
            }
        DialogType.SUCCESSFUL -> provider.provideCompleteInfo()
        DialogType.COMPLETE_ERROR -> provider.provideOnCompleteErrorInfo()
    }

    private fun getIcon(type: DialogType) =
        when (type) {
            DialogType.ERROR, DialogType.COMPLETE_ERROR -> provider.provideRedAlertIcon()
            DialogType.MANDATORY -> provider.provideSavedIcon()
            DialogType.WARNING -> provider.provideYellowAlertIcon()
            DialogType.SUCCESSFUL -> provider.provideSavedIcon()
        }

    private fun getMainButton(
        type: DialogType,
        eventState: EventStatus?,
    ) = when (type) {
        DialogType.ERROR,
        DialogType.MANDATORY,
        DialogType.COMPLETE_ERROR,
        -> MainButton(R.string.review)

        DialogType.WARNING ->
            if (eventState == EventStatus.COMPLETED || eventState == null) {
                MainButton(R.string.review)
            } else {
                CompleteButton
            }
        DialogType.SUCCESSFUL,
        -> CompleteButton
    }

    private fun getFieldsWithIssues(
        errorFields: List<FieldWithIssue> = emptyList(),
        mandatoryFields: List<String> = emptyList(),
        warningFields: List<FieldWithIssue> = emptyList(),
        onCompleteFields: List<FieldWithIssue> = emptyList(),
    ): List<FieldWithIssue> =
        onCompleteFields
            .plus(errorFields)
            .plus(
                mandatoryFields.map {
                    FieldWithIssue(
                        "uid",
                        it,
                        IssueType.MANDATORY,
                        provider.provideMandatoryField(),
                    )
                },
            ).plus(warningFields)

    private fun getOnCompleteMessage(
        canComplete: Boolean,
        onCompleteMessage: String?,
    ): List<FieldWithIssue> {
        val issueOnComplete =
            onCompleteMessage?.let {
                FieldWithIssue(
                    fieldUid = "",
                    fieldName = it,
                    issueType =
                        when (canComplete) {
                            false -> IssueType.ERROR_ON_COMPLETE
                            else -> IssueType.WARNING_ON_COMPLETE
                        },
                    message = "",
                )
            }
        return issueOnComplete?.let { listOf(it) } ?: emptyList()
    }

    private fun getDialogType(
        errorFields: List<FieldWithIssue>,
        mandatoryFields: Map<String, String>,
        warningFields: List<FieldWithIssue>,
        onCompleteFields: List<FieldWithIssue>,
    ) = when {
        onCompleteFields.any { it.issueType == IssueType.ERROR_ON_COMPLETE } ->
            DialogType.COMPLETE_ERROR
        errorFields.isNotEmpty() -> DialogType.ERROR
        mandatoryFields.isNotEmpty() -> DialogType.MANDATORY
        warningFields.isNotEmpty() ||
            onCompleteFields.any { it.issueType == IssueType.WARNING_ON_COMPLETE } ->
            DialogType.WARNING
        else -> DialogType.SUCCESSFUL
    }

    enum class DialogType { ERROR, MANDATORY, WARNING, SUCCESSFUL, COMPLETE_ERROR }
}
