package org.dhis2.form.ui.provider

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.commons.dialogs.bottomsheet.IssueType
import org.dhis2.form.R
import org.dhis2.form.data.EventResultDetails
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.form.model.EventMode
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.event.EventStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class FormResultDialogProviderTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val resourceProvider: FormResultDialogResourcesProvider =
        mock {
            on { provideNotSavedText() } doReturn "not_saved"
            on { provideSavedText() } doReturn "saved"
            on { provideErrorInfo() } doReturn "missing_error_fields_events"
            on { provideMandatoryInfo() } doReturn "missing_mandatory_fields_events"
            on { provideMandatoryField() } doReturn "field_is_mandatory"
            on { provideWarningInfo() } doReturn "missing_warning_fields_events"
            on { provideCompleteInfo() } doReturn "event_can_be_completed"
            on { provideOnCompleteErrorInfo() } doReturn "event_error_on_complete"
        }

    private val formResultDialogProvider =
        FormResultDialogProvider(resourceProvider)

    @Test
    fun `Should not allow to save with missing mandatory fields  in completed events`() {
        val mandatoryFields = mapOf("uid" to "uid")
        val completedEventWithMissingMandatoryFields =
            MissingMandatoryResult(
                mandatoryFields = mandatoryFields,
                errorFields = listOf(),
                warningFields = listOf(),
                canComplete = false,
                onCompleteMessage = null,
                allowDiscard = false,
                eventResultDetails =
                    EventResultDetails(
                        eventStatus = EventStatus.COMPLETED,
                        eventMode = EventMode.CHECK,
                        validationStrategy = ValidationStrategy.ON_COMPLETE,
                    ),
            )
        val canOnlyReviewModel =
            formResultDialogProvider.invoke(
                canComplete = completedEventWithMissingMandatoryFields.canComplete,
                onCompleteMessage = completedEventWithMissingMandatoryFields.onCompleteMessage,
                errorFields = completedEventWithMissingMandatoryFields.errorFields,
                emptyMandatoryFields = completedEventWithMissingMandatoryFields.mandatoryFields,
                warningFields = completedEventWithMissingMandatoryFields.warningFields,
                eventMode = completedEventWithMissingMandatoryFields.eventResultDetails.eventMode ?: EventMode.NEW,
                eventState = completedEventWithMissingMandatoryFields.eventResultDetails.eventStatus ?: EventStatus.ACTIVE,
                result = completedEventWithMissingMandatoryFields,
            )

        val completedEventWithNoErrors =
            SuccessfulResult(
                canComplete = false,
                onCompleteMessage = null,
                eventResultDetails =
                    EventResultDetails(
                        eventStatus = EventStatus.COMPLETED,
                        eventMode = EventMode.CHECK,
                        validationStrategy = ValidationStrategy.ON_COMPLETE,
                    ),
            )
        assertTrue(canOnlyReviewModel.first.mainButton == DialogButtonStyle.MainButton(R.string.review))
        assertTrue(canOnlyReviewModel.first.secondaryButton == null)
    }

    @Test
    fun `Should show complete button if event can be completed`() {
        val completedEventWithNoErrors =
            SuccessfulResult(
                canComplete = true,
                onCompleteMessage = null,
                eventResultDetails =
                    EventResultDetails(
                        eventStatus = EventStatus.ACTIVE,
                        eventMode = EventMode.CHECK,
                        validationStrategy = ValidationStrategy.ON_COMPLETE,
                    ),
            )
        val noErrorsInFormModel =
            formResultDialogProvider.invoke(
                canComplete = completedEventWithNoErrors.canComplete,
                onCompleteMessage = completedEventWithNoErrors.onCompleteMessage,
                errorFields = emptyList(),
                emptyMandatoryFields = emptyMap(),
                warningFields = emptyList(),
                eventMode = completedEventWithNoErrors.eventResultDetails.eventMode ?: EventMode.NEW,
                eventState = completedEventWithNoErrors.eventResultDetails.eventStatus ?: EventStatus.ACTIVE,
                result = completedEventWithNoErrors,
            )
        assertTrue(noErrorsInFormModel.first.mainButton == DialogButtonStyle.CompleteButton)
    }

    @Test
    fun `Should configure to show warning on complete message`() {
        val completedEventWithNoErrors =
            SuccessfulResult(
                canComplete = true,
                onCompleteMessage = "Warning on complete",
                eventResultDetails =
                    EventResultDetails(
                        eventStatus = EventStatus.ACTIVE,
                        eventMode = EventMode.CHECK,
                        validationStrategy = ValidationStrategy.ON_COMPLETE,
                    ),
            )
        val noErrorsInFormModel =
            formResultDialogProvider.invoke(
                canComplete = completedEventWithNoErrors.canComplete,
                onCompleteMessage = completedEventWithNoErrors.onCompleteMessage,
                errorFields = emptyList(),
                emptyMandatoryFields = emptyMap(),
                warningFields = emptyList(),
                eventMode = completedEventWithNoErrors.eventResultDetails.eventMode ?: EventMode.NEW,
                eventState = completedEventWithNoErrors.eventResultDetails.eventStatus ?: EventStatus.ACTIVE,
                result = completedEventWithNoErrors,
            )
        assertTrue(noErrorsInFormModel.second.first().issueType == IssueType.WARNING_ON_COMPLETE)
    }

    @Test
    fun `Should configure to show error on complete message`() {
        val completedEventWithNoErrors =
            SuccessfulResult(
                canComplete = false,
                onCompleteMessage = "error on complete",
                eventResultDetails =
                    EventResultDetails(
                        eventStatus = EventStatus.ACTIVE,
                        eventMode = EventMode.NEW,
                        validationStrategy = ValidationStrategy.ON_COMPLETE,
                    ),
            )
        val noErrorsInFormModel =
            formResultDialogProvider.invoke(
                canComplete = completedEventWithNoErrors.canComplete,
                onCompleteMessage = completedEventWithNoErrors.onCompleteMessage,
                errorFields = emptyList(),
                emptyMandatoryFields = emptyMap(),
                warningFields = emptyList(),
                eventMode = completedEventWithNoErrors.eventResultDetails.eventMode ?: EventMode.NEW,
                eventState = completedEventWithNoErrors.eventResultDetails.eventStatus ?: EventStatus.ACTIVE,
                result = completedEventWithNoErrors,
            )
        assertTrue(noErrorsInFormModel.second.first().issueType == IssueType.ERROR_ON_COMPLETE)
    }

    @Test
    fun `Should follow validation strategy when trying to save the form with errors`() {
        val completedEventWithNoErrors =
            SuccessfulResult(
                canComplete = true,
                onCompleteMessage = null,
                eventResultDetails =
                    EventResultDetails(
                        eventStatus = EventStatus.ACTIVE,
                        eventMode = EventMode.CHECK,
                        validationStrategy = ValidationStrategy.ON_COMPLETE,
                    ),
            )
        val mandatoryFields = mapOf("uid" to "uid")
        val resultWithErrorsButCanSave =
            MissingMandatoryResult(
                mandatoryFields = mandatoryFields,
                errorFields = listOf(),
                warningFields = listOf(),
                canComplete = true,
                onCompleteMessage = null,
                allowDiscard = false,
                eventResultDetails =
                    EventResultDetails(
                        eventStatus = EventStatus.ACTIVE,
                        eventMode = EventMode.CHECK,
                        validationStrategy = ValidationStrategy.ON_COMPLETE,
                    ),
            )
        val resultWithErrorsButCanNotSave =
            MissingMandatoryResult(
                mandatoryFields = mandatoryFields,
                errorFields = listOf(),
                warningFields = listOf(),
                canComplete = true,
                onCompleteMessage = null,
                allowDiscard = false,
                eventResultDetails =
                    EventResultDetails(
                        eventStatus = EventStatus.ACTIVE,
                        eventMode = EventMode.CHECK,
                        validationStrategy = ValidationStrategy.ON_UPDATE_AND_INSERT,
                    ),
            )

        val noErrorsInFormModel =
            formResultDialogProvider.invoke(
                canComplete = completedEventWithNoErrors.canComplete,
                onCompleteMessage = completedEventWithNoErrors.onCompleteMessage,
                errorFields = emptyList(),
                emptyMandatoryFields = emptyMap(),
                warningFields = emptyList(),
                eventMode = completedEventWithNoErrors.eventResultDetails.eventMode ?: EventMode.NEW,
                eventState = completedEventWithNoErrors.eventResultDetails.eventStatus ?: EventStatus.ACTIVE,
                result = completedEventWithNoErrors,
            )
        assertTrue(noErrorsInFormModel.first.mainButton == DialogButtonStyle.CompleteButton)

        val validationStrategyOnUpdateModel =
            formResultDialogProvider.invoke(
                canComplete = resultWithErrorsButCanNotSave.canComplete,
                onCompleteMessage = resultWithErrorsButCanNotSave.onCompleteMessage,
                errorFields = emptyList(),
                emptyMandatoryFields = mandatoryFields,
                warningFields = emptyList(),
                eventMode = resultWithErrorsButCanNotSave.eventResultDetails.eventMode ?: EventMode.NEW,
                eventState = resultWithErrorsButCanNotSave.eventResultDetails.eventStatus ?: EventStatus.ACTIVE,
                result = resultWithErrorsButCanNotSave,
            )

        assertTrue(validationStrategyOnUpdateModel.first.mainButton == DialogButtonStyle.MainButton(R.string.review))

        val validationStrategyOnCompleteModel =
            formResultDialogProvider.invoke(
                canComplete = resultWithErrorsButCanSave.canComplete,
                onCompleteMessage = resultWithErrorsButCanSave.onCompleteMessage,
                errorFields = emptyList(),
                emptyMandatoryFields = mandatoryFields,
                warningFields = emptyList(),
                eventMode = resultWithErrorsButCanSave.eventResultDetails.eventMode ?: EventMode.NEW,
                eventState = resultWithErrorsButCanSave.eventResultDetails.eventStatus ?: EventStatus.ACTIVE,
                result = resultWithErrorsButCanSave,
            )
        assertTrue(validationStrategyOnCompleteModel.first.mainButton == DialogButtonStyle.MainButton(R.string.review))
        assertTrue(validationStrategyOnCompleteModel.first.secondaryButton == DialogButtonStyle.SecondaryButton(R.string.not_now))
    }
}
