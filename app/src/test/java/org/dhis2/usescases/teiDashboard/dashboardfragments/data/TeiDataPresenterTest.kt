package org.dhis2.usescases.teiDashboard.dashboardfragments.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.commons.bindings.canCreateEventInEnrollment
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.OptionsRepository
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.ui.MetadataIconData
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.usescases.teiDashboard.TeiDashboardPresenter
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
    private val activityPresenter: TeiDashboardPresenter = mock()

    @Before
    fun setUp() {
        teiDataPresenter = TEIDataPresenter(
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
            activityPresenter,
        )
    }

    @Test
    fun `Should return false if orgUnit does not belong to the capture scope`() {
        mockEnrollmentOrgUnitInCaptureScope(false)
        assertTrue(
            teiDataPresenter.enrollmentOrgUnitInCaptureScope("orgUnitUid"),
        )
    }

    private fun mockEnrollmentOrgUnitInCaptureScope(returnValue: Boolean) {
        whenever(
            d2.organisationUnitModule().organisationUnits(),
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE),
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid(),
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq("orgUnitUid"),
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq("orgUnitUid")
                .blockingIsEmpty(),
        ) doReturn returnValue
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
        val mockedEnrollment: Enrollment = mock {
            on { organisationUnit() } doReturn "orgUnitUid"
        }
        whenever(teiDataRepository.getEnrollment())doReturn Single.just(mockedEnrollment)
        mockEnrollmentOrgUnitInCaptureScope(false)
        teiDataPresenter.onEventCreationClick(EventCreationOptionsMapper.ADD_NEW_ID)
        contractLiveData.value = Unit
    }

    @Test
    fun shouldNotBeAbleToCreateNewEventsWhenFull() {
        val mockedEnrollment = mock<Enrollment> {
            on { status() } doReturn EnrollmentStatus.ACTIVE
        }
        whenever(d2.enrollment(enrollmentUid)) doReturn mockedEnrollment
        whenever(d2.canCreateEventInEnrollment(enrollmentUid, emptyList())) doReturn false
        teiDataPresenter.updateCreateEventButtonVisibility(false)
        assertTrue(teiDataPresenter.shouldDisplayEventCreationButton.value == false)
    }

    @Test
    fun shouldNotBeAbleToCreateNewEventsWhenEnrollmentNotActive() {
        val mockedEnrollment = mock<Enrollment> {
            on { status() } doReturn EnrollmentStatus.CANCELLED
        }
        whenever(d2.enrollment(enrollmentUid)) doReturn mockedEnrollment
        whenever(d2.canCreateEventInEnrollment(enrollmentUid, emptyList())) doReturn true
        teiDataPresenter.updateCreateEventButtonVisibility(false)
        assertTrue(teiDataPresenter.shouldDisplayEventCreationButton.value == false)
    }

    @Test
    fun `Should display schedule events dialogs when configured`() {
        val programStage = ProgramStage.builder()
            .uid("programStage")
            .allowGenerateNextVisit(true)
            .displayGenerateEventBox(true)
            .remindCompleted(false)
            .build()
        whenever(
            dashboardRepository.displayGenerateEvent("eventUid"),
        ) doReturn Observable.just(programStage)
        teiDataPresenter.displayGenerateEvent("eventUid")
        verify(view).displayScheduleEvent()
    }

    @Test
    fun `Should display close program dialogs when configured`() {
        val programStage = ProgramStage.builder()
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

    private fun fakeModel(
        eventCount: Int = 0,
        type: EventViewModelType = EventViewModelType.STAGE,
    ): EventViewModel {
        val dataElements = mutableListOf<Pair<String, String>>()
        dataElements.add(
            Pair("Name", "Peter"),
        )

        return EventViewModel(
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
