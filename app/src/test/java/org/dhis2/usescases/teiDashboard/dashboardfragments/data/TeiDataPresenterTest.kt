package org.dhis2.usescases.teiDashboard.dashboardfragments.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.bindings.canCreateEventInEnrollment
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.D2ErrorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.OptionsRepository
import org.dhis2.form.model.EventMode
import org.dhis2.mobile.commons.model.MetadataIconData
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.tracker.events.CreateEventUseCase
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.EventCreationOptionsMapper
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataContracts
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataContractHandler
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import org.dhis2.usescases.teiDashboard.domain.GetNewEventCreationTypeOptions
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.Date

class TeiDataPresenterTest {
    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    private val view: TEIDataContracts.View = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val dashboardRepository: DashboardRepository = mock()
    private val teiDataRepository: TeiDataRepository = mock()
    private val ruleEngineHelper: RuleEngineHelper = mock()
    private val programUid = "UID"
    private val teiUid = "123"
    private val enrollmentUid = "456"
    private val schedulers = TrampolineSchedulerProvider()
    private val analytics: AnalyticsHelper = mock()
    private lateinit var teiDataPresenter: TEIDataPresenter
    private val valueStore: FormValueStore = mock()
    private val optionsRepository: OptionsRepository = mock()
    private val getNewEventCreationTypeOptions: GetNewEventCreationTypeOptions = mock()
    private val resources: ResourceManager = mock()
    private val eventCreationOptionsMapper = EventCreationOptionsMapper(resources)
    private val teiDataContractHandler: TeiDataContractHandler = mock()
    private val dispatcherProvider: DispatcherProvider =
        mock {
            on { io() } doReturn Dispatchers.Unconfined
        }
    private val createEventUseCase: CreateEventUseCase = mock()
    private val d2ErrorUtils: D2ErrorUtils = mock()
    private val preferences: PreferenceProvider = mock()

    @Before
    fun setUp() {
        teiDataPresenter =
            TEIDataPresenter(
                view,
                d2,
                dashboardRepository,
                teiDataRepository,
                ruleEngineHelper,
                programUid,
                teiUid,
                enrollmentUid,
                schedulers,
                analytics,
                valueStore,
                optionsRepository,
                getNewEventCreationTypeOptions,
                eventCreationOptionsMapper,
                teiDataContractHandler,
                dispatcherProvider,
                createEventUseCase,
                d2ErrorUtils,
                preferences,
            )
    }

    @Test
    fun `Should return false if orgUnit does not belong to the capture scope`() {
        whenever(
            teiDataRepository.enrollmentOrgUnitInCaptureScope("orgUnitUid"),
        ) doReturn false
        assertTrue(
            !teiDataPresenter.enrollmentOrgUnitInCaptureScope("orgUnitUid"),
        )
    }

    @Test
    fun `Should show category combo dialog`() {
        whenever(
            teiDataRepository.eventsWithoutCatCombo(),
        ) doReturn Single.just(mock())
        teiDataPresenter.getEventsWithoutCatCombo()
        verify(view).displayCatComboOptionSelectorForEvents(any())
    }

    @Test
    fun `Should return orgUnit display name`() {
        val uid = "orgUnitUid"
        whenever(teiDataRepository.getOrgUnitName(uid)) doReturn "OrgUnitDisplayName"
        assertTrue(teiDataPresenter.getOrgUnitName(uid) == "OrgUnitDisplayName")
    }

    @Test
    fun `Should return null when apply effects of hide program stage with no events`() {
        val stage = fakeModel()
        assert(stage.applyHideStage(true) == null)
        assert(stage.applyHideStage(false) == stage)
    }

    @Test
    fun `Should not be able to add event when apply effects of hide program stage with events`() {
        val stage = fakeModel(3)
        assert(stage.applyHideStage(true)?.canAddNewEvent == false)
    }

    @Test
    fun `Should not apply effects of hide program stage for events`() {
        val stage = fakeModel(0, EventViewModelType.EVENT)
        assert(stage.applyHideStage(true) == stage)
    }

    @Test
    fun shouldSuccessfullyCreateANewEvent() {
        val lifecycleOwner: LifecycleOwner = Mockito.mock(LifecycleOwner::class.java)
        val lifecycle = LifecycleRegistry(Mockito.mock(LifecycleOwner::class.java))
        lifecycle.currentState = Lifecycle.State.RESUMED
        Mockito.`when`(lifecycleOwner.lifecycle).thenReturn(lifecycle)

        val contractLiveData = MutableLiveData<Unit>()
        whenever(view.viewLifecycleOwner()) doReturn lifecycleOwner
        whenever(teiDataContractHandler.createEvent(any())) doReturn contractLiveData
        val mockedEnrollment: Enrollment =
            mock {
                on { organisationUnit() } doReturn "orgUnitUid"
            }
        whenever(teiDataRepository.getEnrollment()) doReturn Single.just(mockedEnrollment)
        whenever(teiDataRepository.enrollmentOrgUnitInCaptureScope("orgUnitUid")) doReturn false
        teiDataPresenter.onEventCreationClick(EventCreationOptionsMapper.ADD_NEW_ID)
        contractLiveData.value = Unit
    }

    @Test
    fun shouldNotBeAbleToCreateNewEventsWhenFull() {
        val mockedEnrollment =
            mock<Enrollment> {
                on { status() } doReturn EnrollmentStatus.ACTIVE
            }
        whenever(d2.enrollment(enrollmentUid)) doReturn mockedEnrollment
        whenever(d2.canCreateEventInEnrollment(enrollmentUid, emptyList())) doReturn false
        teiDataPresenter.updateCreateEventButtonVisibility()
        assertTrue(teiDataPresenter.shouldDisplayEventCreationButton.value == false)
    }

    @Test
    fun shouldNotBeAbleToCreateNewEventsWhenEnrollmentNotActive() {
        val mockedEnrollment =
            mock<Enrollment> {
                on { status() } doReturn EnrollmentStatus.CANCELLED
            }
        whenever(d2.enrollment(enrollmentUid)) doReturn mockedEnrollment
        whenever(d2.canCreateEventInEnrollment(enrollmentUid, emptyList())) doReturn true
        teiDataPresenter.updateCreateEventButtonVisibility()
        assertTrue(teiDataPresenter.shouldDisplayEventCreationButton.value == false)
    }

    @Test
    fun `Should display schedule events dialogs when configured`() {
        val programStage =
            ProgramStage
                .builder()
                .uid("programStage")
                .allowGenerateNextVisit(true)
                .displayGenerateEventBox(true)
                .remindCompleted(false)
                .build()
        whenever(
            dashboardRepository.displayGenerateEvent("eventUid"),
        ) doReturn Observable.just(programStage)
        teiDataPresenter.displayGenerateEvent("eventUid")
        verify(view).displayScheduleEvent(programStage = null, showYesNoOptions = true, eventCreationType = EventCreationType.SCHEDULE)
    }

    @Test
    fun `Should display close program dialogs when configured`() {
        val programStage =
            ProgramStage
                .builder()
                .uid("programStage")
                .allowGenerateNextVisit(false)
                .displayGenerateEventBox(false)
                .remindCompleted(true)
                .build()
        whenever(
            dashboardRepository.displayGenerateEvent("eventUid"),
        ) doReturn Observable.just(programStage)
        teiDataPresenter.displayGenerateEvent("eventUid")
        verify(view).showDialogCloseProgram()
    }

    @Test
    fun `Should not show ORG unit selector dialog when org count is 1`() =
        runBlocking {
            val orgUnitUid = "orgUnitUid"
            val programStageUid = "programStageUid"
            val eventUid = "eventUid"

            val orgUnit =
                OrganisationUnit
                    .builder()
                    .uid(orgUnitUid)
                    .build()

            whenever(
                teiDataRepository.programOrgListInCaptureScope(programUid),
            ) doReturn listOf(orgUnit)

            whenever(
                createEventUseCase.invoke(
                    programUid,
                    orgUnitUid,
                    programStageUid,
                    enrollmentUid,
                ),
            ) doReturn (Result.success(eventUid))

            teiDataPresenter.checkOrgUnitCount(programUid, programStageUid)

            verify(view).goToEventDetails(eventUid, EventMode.NEW, programUid)
            verifyNoMoreInteractions(view)
        }

    @Test
    fun `Should show ORG unit selector dialog when org count is greater than 1`() =
        runBlocking {
            val orgUnitUid1 = "orgUnitUid 1"
            val orgUnitUid2 = "orgUnitUid 2"
            val programStageUid = "programStageUid"

            val orgUnit1 =
                OrganisationUnit
                    .builder()
                    .uid(orgUnitUid1)
                    .build()

            val orgUnit2 =
                OrganisationUnit
                    .builder()
                    .uid(orgUnitUid2)
                    .build()

            whenever(
                teiDataRepository.programOrgListInCaptureScope(programUid),
            ) doReturn listOf(orgUnit1, orgUnit2)

            teiDataPresenter.checkOrgUnitCount(programUid, programStageUid)

            verify(view).displayOrgUnitSelectorForNewEvent(programUid, programStageUid)
            verifyNoMoreInteractions(view)
        }

    @Test
    fun `onOrgUnitForNewEventSelected success`() =
        runBlocking {
            val orgUnitUid = "orgUnitUid"
            val programStageUid = "programStageUid"
            val eventUid = "eventUid"

            whenever(
                createEventUseCase.invoke(
                    programUid,
                    orgUnitUid,
                    programStageUid,
                    enrollmentUid,
                ),
            ) doReturn (Result.success(eventUid))

            teiDataPresenter.onNewEventSelected(
                orgUnitUid,
                programStageUid,
            )

            verify(view).goToEventDetails(eventUid, EventMode.NEW, programUid)
            verifyNoMoreInteractions(view)
        }

    @Test
    fun `onOrgUnitForNewEventSelected failure`() =
        runBlocking {
            val orgUnitUid = "orgUnitUid"
            val programStageUid = "programStageUid"
            val errorMessage = "Error message"
            val d2Error =
                D2Error
                    .builder()
                    .errorCode(D2ErrorCode.UNEXPECTED)
                    .errorDescription(errorMessage)
                    .build()

            whenever(
                createEventUseCase.invoke(
                    programUid,
                    orgUnitUid,
                    programStageUid,
                    enrollmentUid,
                ),
            ) doReturn (Result.failure(d2Error))

            whenever(d2ErrorUtils.getErrorMessage(d2Error)) doReturn (errorMessage)

            teiDataPresenter.onNewEventSelected(
                orgUnitUid,
                programStageUid,
            )

            verify(view).displayMessage(errorMessage)
            verifyNoMoreInteractions(view)
        }

    @Test
    fun `should display schedule event without yes and no options, when schedule event option is selected`() {
        // given
        val programStage = ProgramStage.builder().uid("stage").build()

        // when
        teiDataPresenter.onAddNewEventOptionSelected(
            eventCreationType = EventCreationType.SCHEDULE,
            stage = programStage,
        )

        // then
        verify(
            view,
        ).displayScheduleEvent(programStage = programStage, showYesNoOptions = false, eventCreationType = EventCreationType.SCHEDULE)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `should create event in enrollment when ADDNEW is selected with null stage`() {
        // given
        val lifecycleOwner: LifecycleOwner = Mockito.mock(LifecycleOwner::class.java)
        val lifecycle = LifecycleRegistry(Mockito.mock(LifecycleOwner::class.java))
        lifecycle.currentState = Lifecycle.State.RESUMED
        Mockito.`when`(lifecycleOwner.lifecycle).thenReturn(lifecycle)

        val contractLiveData = MutableLiveData<Unit>()
        whenever(view.viewLifecycleOwner()) doReturn lifecycleOwner
        whenever(teiDataContractHandler.createEvent(any())) doReturn contractLiveData

        val mockedEnrollment: Enrollment =
            mock {
                on { organisationUnit() } doReturn "orgUnitUid"
            }
        whenever(teiDataRepository.getEnrollment()) doReturn Single.just(mockedEnrollment)
        whenever(teiDataRepository.enrollmentOrgUnitInCaptureScope("orgUnitUid")) doReturn true

        // when
        teiDataPresenter.onAddNewEventOptionSelected(
            eventCreationType = EventCreationType.ADDNEW,
            stage = null,
        )
        contractLiveData.value = Unit

        // then
        verify(teiDataContractHandler).createEvent(any())
    }

    private fun fakeModel(
        eventCount: Int = 0,
        type: EventViewModelType = EventViewModelType.STAGE,
    ): EventModel {
        val dataElements = mutableListOf<Pair<String, String>>()
        dataElements.add(
            Pair("Name", "Peter"),
        )

        return EventModel(
            type = type,
            stage = ProgramStage.builder().uid("stage").build(),
            event = null,
            eventCount = eventCount,
            lastUpdate = Date(),
            isSelected = false,
            canAddNewEvent = true,
            orgUnitName = "Org unit name",
            catComboName = "Cat combo name",
            dataElementValues = dataElements,
            groupedByStage = null,
            valueListIsOpen = false,
            showTopShadow = false,
            showBottomShadow = false,
            displayDate = "21/11/2023",
            nameCategoryOptionCombo = "Name Category option combo",
            metadataIconData = MetadataIconData.defaultIcon(),
        )
    }
}
