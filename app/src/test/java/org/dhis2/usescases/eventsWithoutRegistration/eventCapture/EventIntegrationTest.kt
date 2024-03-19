package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Flowable
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.ui.dialogs.bottomsheet.IssueType
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ReOpenEventUseCase
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormPresenter
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormView
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCompletionDialog
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.provider.EventCaptureResourcesProvider
import org.dhis2.utils.customviews.FormBottomDialog
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.event.EventStatus
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class EventIntegrationTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val eventCaptureFormView: EventCaptureFormView = mock {}
    private val eventUid = "eventUid"
    private val eventCaptureView: EventCaptureContract.View = mock()
    private val eventRepository: EventCaptureContract.EventCaptureRepository = mock()
    private val schedulers = TrampolineSchedulerProvider()
    private val preferences: PreferenceProvider = mock()
    private val resourceManager: ResourceManager = mock()
    private val reOpenUseCase: ReOpenEventUseCase = mock()
    private val dispatcherProvider: DispatcherProvider = mock()

    private val resourceProvider: EventCaptureResourcesProvider = mock {
        on { provideNotSavedText() } doReturn "not_saved"
        on { provideSavedText() } doReturn "saved"
        on { provideErrorInfo() } doReturn "missing_error_fields_events"
        on { provideMandatoryInfo() } doReturn "missing_mandatory_fields_events"
        on { provideMandatoryField() } doReturn "field_is_mandatory"
        on { provideWarningInfo() } doReturn "missing_warning_fields_events"
        on { provideCompleteInfo() } doReturn "event_can_be_completed"
        on { provideOnCompleteErrorInfo() } doReturn "event_error_on_complete"
    }

    private val configurationEventCompletionDialog =
        ConfigureEventCompletionDialog(resourceProvider)

    private val eventCapturePresenter = EventCapturePresenterImpl(
        view = eventCaptureView,
        eventUid = eventUid,
        eventCaptureRepository = eventRepository,
        schedulerProvider = schedulers,
        preferences = preferences,
        configureEventCompletionDialog = configurationEventCompletionDialog,
    )

    private val eventCaptureFormPresenter = EventCaptureFormPresenter(
        view = eventCaptureFormView,
        activityPresenter = eventCapturePresenter,
        d2 = d2,
        eventUid = eventUid,
        resourceManager = resourceManager,
        reOpenEventUseCase = reOpenUseCase,
        dispatcherProvider = dispatcherProvider,
    )

    @Test
    fun `Should not configure secondary action for mandatory fields`() {
        whenever(
            eventRepository.eventStatus(),
        ) doReturn Flowable.just(EventStatus.ACTIVE)

        whenever(
            eventRepository.validationStrategy(),
        ) doReturn ValidationStrategy.ON_UPDATE_AND_INSERT

        val mandatoryFields = mapOf("uid" to "message")
        val expectedDialog = EventCompletionDialog(
            bottomSheetDialogUiModel = BottomSheetDialogUiModel(
                title = "saved",
                subtitle = null,
                message = "missing_mandatory_fields_events",
                clickableWord = null,
                iconResource = 0,
                mainButton = DialogButtonStyle.MainButton(textResource = 0),
                secondaryButton = null,
            ),
            mainButtonAction = FormBottomDialog.ActionType.CHECK_FIELDS,
            secondaryButtonAction = null,
            fieldsWithIssues = listOf(
                FieldWithIssue(
                    fieldUid = "uid",
                    fieldName = "uid",
                    issueType = IssueType.MANDATORY,
                    message = "field_is_mandatory",
                ),
            ),
        )

        val dataCheckResult = MissingMandatoryResult(
            mandatoryFields = mandatoryFields,
            errorFields = listOf(),
            warningFields = listOf(),
            canComplete = false,
            onCompleteMessage = null,
            allowDiscard = false,
        )
        eventCaptureFormPresenter.handleDataIntegrityResult(dataCheckResult)

        verify(eventCaptureView).showCompleteActions(
            false,
            mandatoryFields,
            expectedDialog,
        )
    }

    @Test
    fun `Should not set secondary button if there are errors`() {
        whenever(
            eventRepository.eventStatus(),
        ) doReturn Flowable.just(EventStatus.ACTIVE)

        whenever(
            eventRepository.validationStrategy(),
        ) doReturn ValidationStrategy.ON_UPDATE_AND_INSERT

        val errors = listOf(
            FieldWithIssue("fieldUid", "fieldName", IssueType.ERROR, "message"),
        )
        val expectedDialog = EventCompletionDialog(
            bottomSheetDialogUiModel = BottomSheetDialogUiModel(
                title = "not_saved",
                subtitle = null,
                message = "missing_error_fields_events",
                clickableWord = null,
                iconResource = 0,
                mainButton = DialogButtonStyle.MainButton(textResource = 0),
                secondaryButton = null,
            ),
            mainButtonAction = FormBottomDialog.ActionType.CHECK_FIELDS,
            secondaryButtonAction = null,
            fieldsWithIssues = errors,
        )

        val dataCheckResult = FieldsWithErrorResult(
            mandatoryFields = mapOf(),
            fieldUidErrorList = errors,
            warningFields = listOf(),
            canComplete = false,
            onCompleteMessage = null,
            allowDiscard = false,
        )
        eventCaptureFormPresenter.handleDataIntegrityResult(dataCheckResult)

        verify(eventCaptureView).showCompleteActions(
            false,
            mapOf(),
            expectedDialog,
        )
    }

    @Test
    fun `Should set secondary button if there are errors and validation strategy is on complete`() {
        whenever(
            eventRepository.eventStatus(),
        ) doReturn Flowable.just(EventStatus.ACTIVE)

        whenever(
            eventRepository.validationStrategy(),
        ) doReturn ValidationStrategy.ON_COMPLETE

        val errors = listOf(
            FieldWithIssue("fieldUid", "fieldName", IssueType.ERROR, "message"),
        )
        val expectedDialog = EventCompletionDialog(
            bottomSheetDialogUiModel = BottomSheetDialogUiModel(
                title = "not_saved",
                subtitle = null,
                message = "missing_error_fields_events",
                clickableWord = null,
                iconResource = 0,
                mainButton = DialogButtonStyle.MainButton(textResource = 0),
                secondaryButton = DialogButtonStyle.SecondaryButton(textResource = 0),
            ),
            mainButtonAction = FormBottomDialog.ActionType.CHECK_FIELDS,
            secondaryButtonAction = FormBottomDialog.ActionType.FINISH,
            fieldsWithIssues = errors,
        )

        val dataCheckResult = FieldsWithErrorResult(
            mandatoryFields = mapOf(),
            fieldUidErrorList = errors,
            warningFields = listOf(),
            canComplete = false,
            onCompleteMessage = null,
            allowDiscard = false,
        )
        eventCaptureFormPresenter.handleDataIntegrityResult(dataCheckResult)

        verify(eventCaptureView).showCompleteActions(
            false,
            mapOf(),
            expectedDialog,
        )
    }
}
