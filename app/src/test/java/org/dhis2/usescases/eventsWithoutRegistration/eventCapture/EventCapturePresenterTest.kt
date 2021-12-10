package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date
import junit.framework.Assert.assertTrue
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.forms.FormSectionViewModel
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.data.FormValueStore
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Before
import org.junit.Test

class EventCapturePresenterTest {
    private lateinit var presenter: EventCapturePresenterImpl
    private val view: EventCaptureContract.View = mock()
    private val eventUid = "eventUid"
    private val eventRepository: EventCaptureContract.EventCaptureRepository = mock()
    private val valueStore: FormValueStore = mock()
    private val schedulers = TrampolineSchedulerProvider()
    private val preferences: PreferenceProvider = mock()
    private val getNextVisibleSection: GetNextVisibleSection = GetNextVisibleSection()
    private val fieldFactory: FieldViewModelFactory = mock()

    @Before
    fun setUp() {
        presenter = EventCapturePresenterImpl(
            view,
            eventUid,
            eventRepository,
            valueStore,
            schedulers,
            preferences
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
        verify(view).setProgramStage(any())

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
        verify(view).setProgramStage(any())
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
    fun `Should reopen an event`() {
        whenever(eventRepository.canReOpenEvent()) doReturn Single.just(true)
        whenever(eventRepository.reopenEvent()) doReturn true

        presenter.reopenEvent()
        verify(view).showSnackBar(any())
    }

    @Test
    fun `Should display error when trying reopen an event`() {
        whenever(view.context) doReturn mock()
        whenever(view.context.getString(any())) doReturn "message"
        whenever(eventRepository.canReOpenEvent()) doReturn Single.just(false)

        presenter.reopenEvent()
        verify(view).displayMessage(any())
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
    fun `Should return current section if sectionsToHide is empty`() {
        val activeSection = getNextVisibleSection.get("activeSection", sections())
        assertTrue(activeSection == "activeSection")
    }

    @Test
    fun `Should return current when section is last one and hide section is not empty`() {
        val activeSection = getNextVisibleSection.get("sectionUid_3", sections())
        assertTrue(activeSection == "sectionUid_3")
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

    private fun sections(): MutableList<FormSectionViewModel> {
        return arrayListOf(
            FormSectionViewModel.createForSection(
                "eventUid",
                "sectionUid_1",
                "sectionName_1",
                null
            ),
            FormSectionViewModel.createForSection(
                "eventUid",
                "sectionUid_2",
                "sectionName_2",
                null
            ),
            FormSectionViewModel.createForSection(
                "eventUid",
                "sectionUid_3",
                "sectionName_3",
                null
            )
        )
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

        whenever(fieldFactory.sectionProcessor()) doReturn Flowable.just("SectionProcessor")
    }
}
