package org.dhis2.usescases.teiDashboard

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.ui.MetadataIconData
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_TEI
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

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
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()

    @Before
    fun setup() {
        presenter = TeiDashboardPresenter(
            view,
            programUid,
            repository,
            schedulers,
            analyticsHelper,
            preferenceProvider,
            matomoAnalyticsController,
        )
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
            Pair(
                TrackedEntityAttribute.builder().uid("teiAttr").build(),
                TrackedEntityAttributeValue.builder().build(),
            ),
        )
        val trackedEntityAttributeValues = listOf(TrackedEntityAttributeValue.builder().build())
        val orgUnits = listOf(OrganisationUnit.builder().uid("orgUnitUid").build())
        val programs = listOf(
            Pair(
                Program.builder().uid(programUid).build(),
                MetadataIconData.defaultIcon(),
            ),
        )

        whenever(
            repository.getTrackedEntityInstance(teiUid),
        ) doReturn Observable.just(trackedEntityInstance)
        whenever(
            repository.getEnrollment(),
        ) doReturn Observable.just(enrollment)
        whenever(
            repository.getProgramStages(programUid),
        ) doReturn Observable.just(programStages)
        whenever(
            repository.getTEIEnrollmentEvents(programUid, teiUid),
        ) doReturn Observable.just(events)
        whenever(
            repository.getAttributesMap(programUid, teiUid),
        ) doReturn Observable.just(trackedEntityAttributes)
        whenever(
            repository.getTEIAttributeValues(programUid, teiUid),
        ) doReturn Observable.just(trackedEntityAttributeValues)
        whenever(
            repository.getTeiOrgUnits(teiUid, programUid),
        ) doReturn Observable.just(orgUnits)
        whenever(
            repository.getTeiActivePrograms(teiUid, false),
        ) doReturn Observable.just(programs)
        whenever(
            filterManager.asFlowable(),
        ) doReturn Flowable.just(filterManager)

        presenter.setProgram(program)

        assert(presenter.programUid == programUid)
        verify(view).restoreAdapter(programUid)
    }

    @Test
    fun `Should not deleteTei if it doesn't has permission`() {
        whenever(repository.deleteTei()) doReturn Single.just(false)
        presenter.deleteTei()

        verify(view).authorityErrorMessage()
    }

    @Test
    fun `Should deleteTei if it has permission`() {
        whenever(repository.deleteTei()) doReturn Single.just(true)
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
    fun `Should show error message when updating the status of the enrollment returns an error`() {
        whenever(
            repository.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED),
        ) doReturn Observable.just(StatusChangeResultCode.FAILED)

        presenter.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)

        verify(view).displayStatusError(StatusChangeResultCode.FAILED)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show permission error when updating the status of the enrollment`() {
        whenever(
            repository.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED),
        ) doReturn Observable.just(StatusChangeResultCode.WRITE_PERMISSION_FAIL)

        presenter.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)

        verify(view).displayStatusError(StatusChangeResultCode.WRITE_PERMISSION_FAIL)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show active enrollment error when updating the status of the enrollment`() {
        whenever(
            repository.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED),
        ) doReturn Observable.just(StatusChangeResultCode.ACTIVE_EXIST)

        presenter.updateEnrollmentStatus("uid", EnrollmentStatus.COMPLETED)

        verify(view).displayStatusError(StatusChangeResultCode.ACTIVE_EXIST)
        verifyNoMoreInteractions(view)
    }
}
