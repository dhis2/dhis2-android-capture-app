package org.dhis2.usescases.teiDashboard

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.utils.AuthorityException
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_ENROLL
import org.dhis2.utils.analytics.DELETE_TEI
import org.hisp.dhis.android.core.enrollment.Enrollment
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
    private val programUid = "programUid"
    private val teiUid = "teiUid"


    @Before
    fun setup() {
        presenter = TeiDashboardPresenter(
            view,
            teiUid,
            programUid,
            repository,
            schedulers,
            analyticsHelper
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
        )doReturn Observable.just(trackedEntityInstance)
        whenever(
            repository.getEnrollment(programUid, teiUid)
        )doReturn Observable.just(enrollment)
        whenever(
            repository.getProgramStages(programUid)
        )doReturn Observable.just(programStages)
        whenever(
            repository.getTEIEnrollmentEvents(programUid, teiUid)
        )doReturn Observable.just(events)
        whenever(
            repository.getProgramTrackedEntityAttributes(programUid)
        )doReturn Observable.just(trackedEntityAttributes)
        whenever(
            repository.getTEIAttributeValues(programUid, teiUid)
        )doReturn Observable.just(trackedEntityAttributeValues)
        whenever(
            repository.getTeiOrgUnits(teiUid, programUid)
        )doReturn Observable.just(orgUnits)
        whenever(
            repository.getTeiActivePrograms(teiUid, false)
        )doReturn Observable.just(programs)

        presenter.init()

        verify(view).setData(presenter.dashboardProgramModel)
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
        )doReturn Observable.just(trackedEntityInstance)
        whenever(
            repository.getProgramTrackedEntityAttributes(null)
        )doReturn Observable.just(trackedEntityAttributes)
        whenever(
            repository.getTEIAttributeValues(null, teiUid)
        )doReturn Observable.just(trackedEntityAttributeValues)
        whenever(
            repository.getTeiOrgUnits(teiUid, null)
        )doReturn Observable.just(orgUnits)
        whenever(
            repository.getTeiActivePrograms(teiUid, true)
        )doReturn Observable.just(programs)
        whenever(
            repository.getTEIEnrollments(teiUid)
        )doReturn Observable.just(enrollments)

        presenter.init()

        verify(view).setDataWithOutProgram(presenter.dashboardProgramModel)
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
        )doReturn Observable.just(trackedEntityInstance)
        whenever(
            repository.getEnrollment(programUid, teiUid)
        )doReturn Observable.just(enrollment)
        whenever(
            repository.getProgramStages(programUid)
        )doReturn Observable.just(programStages)
        whenever(
            repository.getTEIEnrollmentEvents(programUid, teiUid)
        )doReturn Observable.just(events)
        whenever(
            repository.getProgramTrackedEntityAttributes(programUid)
        )doReturn Observable.just(trackedEntityAttributes)
        whenever(
            repository.getTEIAttributeValues(programUid, teiUid)
        )doReturn Observable.just(trackedEntityAttributeValues)
        whenever(
            repository.getTeiOrgUnits(teiUid, programUid)
        )doReturn Observable.just(orgUnits)
        whenever(
            repository.getTeiActivePrograms(teiUid, false)
        )doReturn Observable.just(programs)

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
        )doReturn Single.error(AuthorityException(null))
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
        verify(view).handleTEIdeletion()
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




}