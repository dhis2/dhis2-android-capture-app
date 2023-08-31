package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.bindings.canSkipErrorFix
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCompletionDialog
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.Date

class EventCapturePresenterTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var presenter: EventCapturePresenterImpl
    private val view: EventCaptureContract.View = mock()
    private val eventUid = "eventUid"
    private val eventRepository: EventCaptureContract.EventCaptureRepository = mock()
    private val schedulers = TrampolineSchedulerProvider()
    private val preferences: PreferenceProvider = mock()
    private val configureEventCompletionDialog: ConfigureEventCompletionDialog = mock()

    @Before
    fun setUp() {
        presenter = EventCapturePresenterImpl(
            view,
            eventUid,
            eventRepository,
            schedulers,
            preferences,
            configureEventCompletionDialog,
        )
    }

    @Test
    fun `Should initialize the event capture form`() {
        initializeMocks()
        whenever(eventRepository.eventIntegrityCheck()) doReturn Flowable.just(true)
        whenever(eventRepository.eventStatus()) doReturn Flowable.just(EventStatus.ACTIVE)
        whenever(eventRepository.isEventEditable("eventUid")) doReturn true

        presenter.init()
        verify(view).renderInitialInfo(any(), any(), any(), any())

        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show integrity alert when opening an event form`() {
        initializeMocks()
        whenever(eventRepository.eventIntegrityCheck()) doReturn Flowable.just(false)
        whenever(eventRepository.eventStatus()) doReturn Flowable.just(EventStatus.ACTIVE)
        whenever(eventRepository.isEventEditable("eventUid")) doReturn true

        presenter.init()
        verify(view).showEventIntegrityAlert()
        verify(view).renderInitialInfo(any(), any(), any(), any())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should go back when click`() {
        presenter.onBackClick()

        verify(view).goBack()
    }

    @Test
    fun `Should return enrollment that is not open`() {
        whenever(eventRepository.isEnrollmentOpen) doReturn false

        val result = presenter.isEnrollmentOpen
        assertTrue(!result)
    }

    @Test
    fun `Should return enrollment that is open`() {
        whenever(eventRepository.isEnrollmentOpen) doReturn true

        val result = presenter.isEnrollmentOpen
        assertTrue(result)
    }

    @Test
    fun `Should complete an event and finish data entry`() {
        whenever(eventRepository.completeEvent()) doReturn Observable.just(true)

        presenter.completeEvent(false)
        verify(view).finishDataEntry()
    }

    @Test
    fun `Should complete an event and restart data entry`() {
        whenever(eventRepository.completeEvent()) doReturn Observable.just(true)

        presenter.completeEvent(true)
        verify(view).restartDataEntry()
    }

    @Test
    fun `Should not complete an event`() {
        whenever(eventRepository.completeEvent()) doReturn Observable.just(false)

        presenter.completeEvent(true)
        verify(view).restartDataEntry()
    }

    @Test
    fun `Should delete an event`() {
        whenever(eventRepository.deleteEvent()) doReturn Observable.just(true)

        presenter.deleteEvent()
        verify(view).showSnackBar(any())
        verify(view).finishDataEntry()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should close form if it could not delete an event`() {
        whenever(eventRepository.deleteEvent()) doReturn Observable.just(false)

        presenter.deleteEvent()
        verify(view).finishDataEntry()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should skip an event`() {
        val status = EventStatus.SKIPPED
        whenever(eventRepository.updateEventStatus(status)) doReturn Observable.just(true)

        presenter.skipEvent()
        verify(view).showSnackBar(any())
        verify(view).finishDataEntry()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should reschedule and event`() {
        whenever(eventRepository.rescheduleEvent(any())) doReturn Observable.just(true)

        presenter.rescheduleEvent(Date())
        verify(view).finishDataEntry()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should return false if user does not have write permission`() {
        whenever(eventRepository.accessDataWrite) doReturn false

        val result = presenter.canWrite()
        assertTrue(!result)
    }

    @Test
    fun `Should return true if user does have write permission`() {
        whenever(eventRepository.accessDataWrite) doReturn true

        val result = presenter.canWrite()
        assertTrue(result)
    }

    @Test
    fun `Should return false if an event has not expired`() {
        presenter.onDettach()

        val result = presenter.compositeDisposable.size()
        assert(result == 0)
    }

    @Test
    fun `Should display message`() {
        presenter.displayMessage("message")

        verify(view).displayMessage(any())
    }

    @Test
    fun `Should hide progress`() {
        presenter.hideProgress()
        verify(view).hideProgress()
    }

    @Test
    fun `Should show progress`() {
        presenter.showProgress()
        verify(view).showProgress()
    }

    @Test
    fun `Should show completion percentage`() {
        whenever(eventRepository.showCompletionPercentage()) doReturn true

        val result = presenter.completionPercentageVisibility
        assertTrue(result)
    }

    @Test
    fun `Should hide completion percentage`() {
        whenever(eventRepository.showCompletionPercentage()) doReturn false

        val result = presenter.completionPercentageVisibility
        assertTrue(!result)
    }

    @Test
    fun `Should update actions`() {
        presenter.emitAction(EventCaptureAction.ON_BACK)
        val result = presenter.observeActions().value
        assertTrue(result == EventCaptureAction.ON_BACK)
    }

    @Test
    fun `Should check completed event expiration`() {
        initializeMocks()
        whenever(eventRepository.eventIntegrityCheck()) doReturn Flowable.just(false)
        whenever(eventRepository.eventStatus()) doReturn Flowable.just(EventStatus.COMPLETED)
        whenever(eventRepository.isEventEditable("eventUid")) doReturn true

        whenever(eventRepository.isCompletedEventExpired(any())) doReturn Observable.just(true)
        whenever(eventRepository.isEventEditable(any())) doReturn false

        presenter.init()

        assertTrue(presenter.hasExpired())
    }

    @Test
    fun `Should set action by status if event is completed and expired`() {
        initializeMocks()
        whenever(eventRepository.eventIntegrityCheck()) doReturn Flowable.just(false)
        whenever(eventRepository.eventStatus()) doReturn Flowable.just(EventStatus.COMPLETED)
        whenever(eventRepository.isEventEditable("eventUid")) doReturn true

        whenever(eventRepository.isCompletedEventExpired(any())) doReturn Observable.just(true)
        whenever(eventRepository.isEventEditable(any())) doReturn true

        presenter.attemptFinish(true, null, emptyList(), emptyMap(), emptyList())

        verify(view).SaveAndFinish()
    }

    @Test
    fun `Should set action by status if event is completed and not expired`() {
        initializeMocks()
        whenever(eventRepository.eventIntegrityCheck()) doReturn Flowable.just(false)
        whenever(eventRepository.eventStatus()) doReturn Flowable.just(EventStatus.COMPLETED)
        whenever(eventRepository.isEventEditable("eventUid")) doReturn true

        presenter.init()

        whenever(eventRepository.isCompletedEventExpired(any())) doReturn Observable.just(false)
        whenever(eventRepository.isEventEditable(any())) doReturn true
        whenever(eventRepository.isEnrollmentCancelled) doReturn true

        presenter.attemptFinish(true, null, emptyList(), emptyMap(), emptyList())

        verify(view).finishDataEntry()
    }

    @Test
    fun `Should set action by status if event is overdue`() {
        initializeMocks()
        whenever(eventRepository.eventIntegrityCheck()) doReturn Flowable.just(false)
        whenever(eventRepository.eventStatus()) doReturn Flowable.just(EventStatus.OVERDUE)
        whenever(eventRepository.isEventEditable("eventUid")) doReturn true

        presenter.init()

        whenever(eventRepository.isCompletedEventExpired(any())) doReturn Observable.just(false)
        whenever(eventRepository.isEventEditable(any())) doReturn true

        presenter.attemptFinish(true, null, emptyList(), emptyMap(), emptyList())

        verify(view).attemptToSkip()
    }

    @Test
    fun `Should set action by status if event is skipped`() {
        initializeMocks()
        whenever(eventRepository.eventIntegrityCheck()) doReturn Flowable.just(false)
        whenever(eventRepository.eventStatus()) doReturn Flowable.just(EventStatus.SKIPPED)
        whenever(eventRepository.isEventEditable("eventUid")) doReturn true

        presenter.init()

        whenever(eventRepository.isCompletedEventExpired(any())) doReturn Observable.just(false)
        whenever(eventRepository.isEventEditable(any())) doReturn true

        presenter.attemptFinish(true, null, emptyList(), emptyMap(), emptyList())

        verify(view).attemptToReschedule()
    }

    @Test
    fun `Should set action by status if event is active`() {
        initializeMocks()
        whenever(eventRepository.eventIntegrityCheck()) doReturn Flowable.just(false)
        whenever(eventRepository.eventStatus()) doReturn Flowable.just(EventStatus.ACTIVE)
        whenever(eventRepository.isEventEditable("eventUid")) doReturn true

        whenever(
            eventRepository.validationStrategy(),
        ) doReturn ValidationStrategy.ON_UPDATE_AND_INSERT
        val eventCompletionDialog: EventCompletionDialog = mock()
        whenever(
            configureEventCompletionDialog.invoke(any(), any(), any(), any(), any(), any()),
        ) doReturn eventCompletionDialog
        whenever(
            eventRepository.isEnrollmentOpen,
        ) doReturn true

        presenter.attemptFinish(
            canComplete = true,
            onCompleteMessage = "Complete",
            errorFields = emptyList(),
            emptyMandatoryFields = emptyMap(),
            warningFields = emptyList(),
        )

        verify(view).showCompleteActions(
            any(),
            any(),
            any(),
        )
        verify(view).showNavigationBar()
    }

    @Test
    fun `Should init note counter`() {
        whenever(eventRepository.noteCount) doReturnConsecutively listOf(
            0,
            1,
        ).map { Single.just(it) }
        presenter.initNoteCounter()
        verify(view).updateNoteBadge(0)
        presenter.initNoteCounter()
        verify(view).updateNoteBadge(1)
    }

    @Test
    fun `Should allow skip error if validation strategy is ON_COMPLETE`() {
        val canSkipErrorFix = ValidationStrategy.ON_COMPLETE.canSkipErrorFix(
            hasErrorFields = false,
            hasEmptyMandatoryFields = false,
        )
        assertTrue(canSkipErrorFix)
    }

    @Test
    fun `Should allow skip error if validation strategy is ON_COMPLETE and has error`() {
        val canSkipErrorFix = ValidationStrategy.ON_COMPLETE.canSkipErrorFix(
            hasErrorFields = true,
            hasEmptyMandatoryFields = false,
        )
        assertTrue(canSkipErrorFix)
    }

    @Test
    fun `Should allow skip error if validation strategy is ON_COMPLETE and has mandatory`() {
        val canSkipErrorFix = ValidationStrategy.ON_COMPLETE.canSkipErrorFix(
            hasErrorFields = false,
            hasEmptyMandatoryFields = true,
        )
        assertTrue(canSkipErrorFix)
    }

    @Test
    fun `Should allow skip error if validation strategy is ON_INSERT if no errors`() {
        val canSkipErrorFix = ValidationStrategy.ON_UPDATE_AND_INSERT.canSkipErrorFix(
            hasErrorFields = false,
            hasEmptyMandatoryFields = false,
        )
        assertTrue(canSkipErrorFix)
    }

    @Test
    fun `Should not allow skip error if validation strategy is ON_INSERT if has errors`() {
        val canSkipErrorFix = ValidationStrategy.ON_UPDATE_AND_INSERT.canSkipErrorFix(
            hasErrorFields = true,
            hasEmptyMandatoryFields = false,
        )
        assertTrue(!canSkipErrorFix)
    }

    @Test
    fun `Should not allow skip error if ON_INSERT and has mandatory fields`() {
        val canSkipErrorFix = ValidationStrategy.ON_UPDATE_AND_INSERT.canSkipErrorFix(
            hasErrorFields = false,
            hasEmptyMandatoryFields = true,
        )
        assertTrue(!canSkipErrorFix)
    }

    private fun initializeMocks() {
        val stage = ProgramStage.builder().uid("stage").displayName("stageName").build()
        val date = "date"
        val orgUnit = OrganisationUnit.builder().uid("orgUnit").displayName("OrgUnitName").build()
        val catOption = "catOption"

        whenever(eventRepository.programStageName()) doReturn Flowable.just(stage.uid())
        whenever(eventRepository.eventDate()) doReturn Flowable.just(date)
        whenever(eventRepository.orgUnit()) doReturn Flowable.just(orgUnit)
        whenever(eventRepository.catOption()) doReturn Flowable.just(catOption)
        doNothing().`when`(preferences).setValue(any(), any())
        whenever(eventRepository.programStage()) doReturn Observable.just(stage.uid())
    }
}
