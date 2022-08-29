package org.dhis2.usescases.teiDashboard

import com.google.gson.reflect.TypeToken
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.Preference.Companion.GROUPING
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.utils.AuthorityException
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_ENROLL
import org.dhis2.utils.analytics.DELETE_TEI
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Before
import org.junit.Test

class TeiDashboardPresenterTest {

    private lateinit var presenter: TeiDashboardPresenter
    private val repository: DashboardRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val view: TeiDashboardContracts.View = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val filterManager: FilterManager = mock()
    private val programUid = "programUid"
    private val teiUid = "teiUid"
    private val enrollmentUid = "enrollmentUid"
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()

    @Before
    fun setup() {
        presenter = TeiDashboardPresenter(
            view,
            teiUid,
            programUid,
            enrollmentUid,
            repository,
            schedulers,
            analyticsHelper,
            preferenceProvider,
            filterManager,
            matomoAnalyticsController
        )
    }

    @Test
    fun `Should set data on init`() {
        val trackedEntityInstance = TrackedEntityInstance.builder().uid(teiUid).build()
        val enrollment = Enrollment.builder().uid("enrollmentUid").build()
        val programStages = listOf(ProgramStage.builder().uid("programStageUid").build())
        val events = listOf(Event.builder().uid("eventUid").build())
        val trackedEntityAttributes = listOf(
            ProgramTrackedEntityAttribute.builder().uid("teiAUid").build()
        )
        val trackedEntityAttributeValues = listOf(TrackedEntityAttributeValue.builder().build())
        val orgUnits = listOf(OrganisationUnit.builder().uid("orgUnitUid").build())
        val programs = listOf(Program.builder().uid(programUid).build())

        whenever(
            repository.getTrackedEntityInstance(teiUid)
        ) doReturn Observable.just(trackedEntityInstance)
        whenever(
            repository.getEnrollment()
        ) doReturn Observable.just(enrollment)
        whenever(
            repository.getProgramStages(programUid)
        ) doReturn Observable.just(programStages)
        whenever(
            repository.getTEIEnrollmentEvents(programUid, teiUid)
        ) doReturn Observable.just(events)
        whenever(
            repository.getProgramTrackedEntityAttributes(programUid)
        ) doReturn Observable.just(trackedEntityAttributes)
        whenever(
            repository.getTEIAttributeValues(programUid, teiUid)
        ) doReturn Observable.just(trackedEntityAttributeValues)
        whenever(
            repository.getTeiOrgUnits(teiUid, programUid)
        ) doReturn Observable.just(orgUnits)
        whenever(
            repository.getTeiActivePrograms(teiUid, false)
        ) doReturn Observable.just(programs)
        whenever(
            filterManager.asFlowable()
        ) doReturn Flowable.just(filterManager)

        presenter.init()

        verify(view).setData(presenter.dashboardProgramModel)
        verify(view, times(2)).updateTotalFilters(any())
    }

    @Test
    fun `Should set data on init without program`() {
        presenter.programUid = null
        val trackedEntityInstance = TrackedEntityInstance.builder().uid(teiUid).build()
        val trackedEntityAttributes = listOf(
            ProgramTrackedEntityAttribute.builder().uid("teiAUid").build()
        )
        val trackedEntityAttributeValues = listOf(TrackedEntityAttributeValue.builder().build())
        val orgUnits = listOf(OrganisationUnit.builder().uid("orgUnitUid").build())
        val programs = listOf(Program.builder().uid(programUid).build())
        val enrollments = listOf(Enrollment.builder().uid("enrollmentUid").build())

        whenever(
            repository.getTrackedEntityInstance(teiUid)
        ) doReturn Observable.just(trackedEntityInstance)
        whenever(
            repository.getProgramTrackedEntityAttributes(null)
        ) doReturn Observable.just(trackedEntityAttributes)
        whenever(
            repository.getTEIAttributeValues(null, teiUid)
        ) doReturn Observable.just(trackedEntityAttributeValues)
        whenever(
            repository.getTeiOrgUnits(teiUid, null)
        ) doReturn Observable.just(orgUnits)
        whenever(
            repository.getTeiActivePrograms(teiUid, true)
        ) doReturn Observable.just(programs)
        whenever(
            repository.getTEIEnrollments(teiUid)
        ) doReturn Observable.just(enrollments)
        whenever(
            filterManager.asFlowable()
        ) doReturn Flowable.just(filterManager)

        presenter.init()

        verify(view).setDataWithOutProgram(presenter.dashboardProgramModel)
        verify(view, times(2)).updateTotalFilters(any())
    }

    @Test
    fun `Should go to enrollment list when clicked`() {
        presenter.onEnrollmentSelectorClick()

        verify(view).goToEnrollmentList()
    }

    @Test
    fun `Should set program and restore adapter`() {
        val programUid = "programUid"
        val program = Program.builder().uid(programUid).build()
        val trackedEntityInstance = TrackedEntityInstance.builder().uid(teiUid).build()
        val enrollment = Enrollment.builder().uid("enrollmentUid").build()
        val programStages = listOf(ProgramStage.builder().uid("programStageUid").build())
        val events = listOf(Event.builder().uid("eventUid").build())
        val trackedEntityAttributes = listOf(
            ProgramTrackedEntityAttribute.builder().uid("teiAUid").build()
        )
        val trackedEntityAttributeValues = listOf(TrackedEntityAttributeValue.builder().build())
        val orgUnits = listOf(OrganisationUnit.builder().uid("orgUnitUid").build())
        val programs = listOf(Program.builder().uid(programUid).build())

        whenever(
            repository.getTrackedEntityInstance(teiUid)
        ) doReturn Observable.just(trackedEntityInstance)
        whenever(
            repository.getEnrollment()
        ) doReturn Observable.just(enrollment)
        whenever(
            repository.getProgramStages(programUid)
        ) doReturn Observable.just(programStages)
        whenever(
            repository.getTEIEnrollmentEvents(programUid, teiUid)
        ) doReturn Observable.just(events)
        whenever(
            repository.getProgramTrackedEntityAttributes(programUid)
        ) doReturn Observable.just(trackedEntityAttributes)
        whenever(
            repository.getTEIAttributeValues(programUid, teiUid)
        ) doReturn Observable.just(trackedEntityAttributeValues)
        whenever(
            repository.getTeiOrgUnits(teiUid, programUid)
        ) doReturn Observable.just(orgUnits)
        whenever(
            repository.getTeiActivePrograms(teiUid, false)
        ) doReturn Observable.just(programs)
        whenever(
            filterManager.asFlowable()
        ) doReturn Flowable.just(filterManager)

        presenter.setProgram(program)

        assert(presenter.programUid == programUid)
        verify(view).restoreAdapter(programUid)
    }

    @Test
    fun `Should not deleteEnrollment if it doesn't has permission`() {
        val currentEnrollment = Enrollment.builder().uid("enrollmentUid").build()
        val dashboardProgramModel = DashboardProgramModel(
            null, currentEnrollment, null, null, null, null, null, null
        )
        presenter.dashboardProgramModel = dashboardProgramModel
        whenever(
            repository.deleteEnrollmentIfPossible(dashboardProgramModel.currentEnrollment.uid())
        ) doReturn Single.error(AuthorityException(null))
        presenter.deleteEnrollment()

        verify(view).authorityErrorMessage()
    }

    @Test
    fun `Should deleteEnrollment if it has permission`() {
        val currentEnrollment = Enrollment.builder().uid("enrollmentUid").build()
        val dashboardProgramModel = DashboardProgramModel(
            null, currentEnrollment, null, null, null, null, null, null
        )
        presenter.dashboardProgramModel = dashboardProgramModel
        whenever(
            repository.deleteEnrollmentIfPossible(dashboardProgramModel.currentEnrollment.uid())
        ) doReturn Single.just(true)
        presenter.deleteEnrollment()

        verify(analyticsHelper).setEvent(DELETE_ENROLL, CLICK, DELETE_ENROLL)
        verify(view).handleEnrollmentDeletion(true)
    }

    @Test
    fun `Should not deleteTei if it doesn't has permission`() {
        whenever(repository.deleteTeiIfPossible()) doReturn Single.just(false)
        presenter.deleteTei()

        verify(view).authorityErrorMessage()
    }

    @Test
    fun `Should deleteTei if it has permission`() {
        whenever(repository.deleteTeiIfPossible()) doReturn Single.just(true)
        presenter.deleteTei()

        verify(analyticsHelper).setEvent(DELETE_TEI, CLICK, DELETE_TEI)
        verify(view).handleTeiDeletion()
    }

    @Test
    fun `Should clear disposable`() {
        presenter.onDettach()

        assert(presenter.compositeDisposable.size() == 0)
    }

    @Test
    fun `Should go back`() {
        presenter.onBackPressed()

        verify(view).back()
    }

    @Test
    fun `Should show description`() {
        presenter.showDescription("description")

        verify(view).showDescription("description")
    }

    @Test
    fun `Should get program id`() {
        val uid = presenter.programUid

        assert(uid == programUid)
    }

    @Test
    fun `Should return true program grouping from preferences if setting set to true`() {
        val typeToken: TypeToken<HashMap<String, Boolean>> =
            object : TypeToken<HashMap<String, Boolean>>() {}
        val returnedHashMap = hashMapOf(programUid to true)

        whenever(
            preferenceProvider.getObjectFromJson(GROUPING, typeToken, hashMapOf())
        ) doReturn returnedHashMap

        val isGrouped = presenter.programGrouping

        assert(isGrouped == true)
    }

    @Test
    fun `Should return false program grouping from preferences if setting is set to false`() {
        val typeToken: TypeToken<HashMap<String, Boolean>> =
            object : TypeToken<HashMap<String, Boolean>>() {}
        val returnedHashMap = hashMapOf(programUid to false)

        whenever(
            preferenceProvider.getObjectFromJson(GROUPING, typeToken, hashMapOf())
        ) doReturn returnedHashMap

        val isGrouped = presenter.programGrouping

        assert(isGrouped == false)
    }

    @Test
    fun `Should return true program grouping if the programUid not = presenter's programUid`() {
        val typeToken: TypeToken<HashMap<String, Boolean>> =
            object : TypeToken<HashMap<String, Boolean>>() {}
        val returnedHashMap = hashMapOf("otherProgramUid" to true)

        whenever(
            preferenceProvider.getObjectFromJson(GROUPING, typeToken, hashMapOf())
        ) doReturn returnedHashMap

        val isGrouped = presenter.programGrouping

        assert(isGrouped == true)
    }

    @Test
    fun `Should return false as program grouping if program uid is null`() {
        presenter = TeiDashboardPresenter(
            view,
            teiUid,
            null,
            enrollmentUid,
            repository,
            schedulers,
            analyticsHelper,
            preferenceProvider,
            filterManager,
            matomoAnalyticsController
        )

        val isGrouped = presenter.programGrouping

        assert(isGrouped == false)
    }

    @Test
    fun `Should handle filters icon click`() {
        presenter.generalFiltersClick()

        verify(view).setFiltersLayoutState()
    }

    @Test
    fun `Should handle if it has to hide the tabs and disable swipe`() {
        presenter.handleShowHideFilters(true)

        verify(view).hideTabsAndDisableSwipe()
    }

    @Test
    fun `Should handle if it has to show the tabs and enable swipe`() {
        presenter.handleShowHideFilters(false)

        verify(view).showTabsAndEnableSwipe()
    }

    @Test
    fun `Should return the status of the enrollment`() {
        presenter.getEnrollmentStatus("uid")

        verify(repository).getEnrollmentStatus(any())
    }

    @Test
    fun `Should update the status of the enrollment`() {
        whenever(
            repository.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)
        ) doReturn Observable.just(StatusChangeResultCode.CHANGED)

        presenter.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)

        verify(view).updateStatus()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show error message when updating the status of the enrollment returns an error`() {
        whenever(
            repository.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)
        ) doReturn Observable.just(StatusChangeResultCode.FAILED)

        presenter.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)

        verify(view).displayStatusError(StatusChangeResultCode.FAILED)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show permission error when updating the status of the enrollment`() {
        whenever(
            repository.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)
        ) doReturn Observable.just(StatusChangeResultCode.WRITE_PERMISSION_FAIL)

        presenter.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)

        verify(view).displayStatusError(StatusChangeResultCode.WRITE_PERMISSION_FAIL)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show active enrollment error when updating the status of the enrollment`() {
        whenever(
            repository.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)
        ) doReturn Observable.just(StatusChangeResultCode.ACTIVE_EXIST)

        presenter.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)

        verify(view).displayStatusError(StatusChangeResultCode.ACTIVE_EXIST)
        verifyNoMoreInteractions(view)
    }
}
