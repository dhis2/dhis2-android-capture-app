package org.dhis2.form.ui.provider

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.dhis2.form.R
import org.dhis2.form.data.EventResultDetails
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.model.EventMode
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.event.EventStatus
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class EventCompletionDialogProviderTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val resourceProvider: CompleteEventDialogResourcesProvider = mock {
        on { provideNotSavedText() } doReturn "not_saved"
        on { provideSavedText() } doReturn "saved"
        on { provideErrorInfo() } doReturn "missing_error_fields_events"
        on { provideMandatoryInfo() } doReturn "missing_mandatory_fields_events"
        on { provideMandatoryField() } doReturn "field_is_mandatory"
        on { provideWarningInfo() } doReturn "missing_warning_fields_events"
        on { provideCompleteInfo() } doReturn "event_can_be_completed"
        on { provideOnCompleteErrorInfo() } doReturn "event_error_on_complete"
    }

    private val eventCompletionDialogProvider =
        EventCompletionDialogProvider(resourceProvider)

    @Test
    fun `Should not configure secondary action for mandatory fields`() {
        val expectedDialog = BottomSheetDialogUiModel(
            title = "saved",
            subtitle = null,
            message = "missing_mandatory_fields_events",
            clickableWord = null,
            iconResource = 0,
            mainButton = DialogButtonStyle.MainButton(textResource = 0),
            secondaryButton = null,
        )
        val mandatoryFields = mapOf("uid" to "uid")
        val dataCheckResult = MissingMandatoryResult(
            mandatoryFields = mandatoryFields,
            errorFields = listOf(),
            warningFields = listOf(),
            canComplete = false,
            onCompleteMessage = null,
            allowDiscard = false,

            eventResultDetails = EventResultDetails(
                eventStatus = EventStatus.ACTIVE,
                eventMode = EventMode.NEW,
                validationStrategy = ValidationStrategy.ON_COMPLETE,
            ),
        )
        val realModel = eventCompletionDialogProvider.invoke(
            canComplete = dataCheckResult.canComplete,
            onCompleteMessage = dataCheckResult.onCompleteMessage,
            errorFields = dataCheckResult.errorFields,
            emptyMandatoryFields = dataCheckResult.mandatoryFields,
            warningFields = dataCheckResult.warningFields,
            eventMode = dataCheckResult.eventResultDetails.eventMode ?: EventMode.NEW,
            eventState = dataCheckResult.eventResultDetails.eventStatus ?: EventStatus.ACTIVE,
            result = dataCheckResult,
        )
        assert(realModel?.first?.mainButton == DialogButtonStyle.MainButton(R.string.review))
    }
}
