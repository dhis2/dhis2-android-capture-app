package org.dhis2.usescases.teiDashboard.teiProgramList

import android.graphics.Color
import org.dhis2.commons.R
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.ui.MetadataIconData
import org.dhis2.usescases.main.program.ProgramDownloadState
import org.dhis2.usescases.main.program.ProgramViewModel
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentService
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TeiProgramListPresenterTest {

    lateinit var presenter: TeiProgramListPresenter
    private val view: TeiProgramListContract.View = mock()
    private val interactor: TeiProgramListContract.Interactor = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val enrollmentService: EnrollmentService = mock()

    @Before
    fun setUp() {
        presenter = TeiProgramListPresenter(
            view,
            interactor,
            "teiUid",
            preferenceProvider,
            analyticsHelper,
            enrollmentService,
        )
    }

    @Test
    fun `Should init interactor`() {
        presenter.init()
        verify(interactor).init(view, "teiUid")
    }

    @Test
    fun `Should call back`() {
        presenter.onBackClick()
        verify(view).back()
    }

    @Test
    fun `Should enroll for write access in program`() {
        whenever(
            enrollmentService.blockingGetEnrollmentAccess(
                anyString(),
                anyString(),
            ),
        ) doReturn EnrollmentAccess.WRITE_ACCESS
        presenter.onEnrollClick(
            mockedProgramViewModel(),
        )
        verify(analyticsHelper).setEvent(anyString(), anyString(), anyString())
        verify(preferenceProvider).removeValue(anyString())
        verify(interactor).enroll(anyString(), anyString())
    }

    @Test
    fun `Should show message for closed program`() {
        val testingProgram = mockedProgramViewModel()
        whenever(
            enrollmentService.blockingGetEnrollmentAccess(
                anyString(),
                anyString(),
            ),
        ) doReturn EnrollmentAccess.CLOSED_PROGRAM_DENIED
        presenter.onEnrollClick(testingProgram)
        verify(view).displayBreakGlassError(testingProgram.typeName)
    }

    @Test
    fun `Should show message for protected program`() {
        val testingProgram = mockedProgramViewModel()
        whenever(
            enrollmentService.blockingGetEnrollmentAccess(
                anyString(),
                anyString(),
            ),
        ) doReturn EnrollmentAccess.PROTECTED_PROGRAM_DENIED
        presenter.onEnrollClick(testingProgram)
        verify(view).displayBreakGlassError(testingProgram.typeName)
    }

    @Test
    fun `Should show message for no access in program`() {
        whenever(
            enrollmentService.blockingGetEnrollmentAccess(
                anyString(),
                anyString(),
            ),
        ) doReturn EnrollmentAccess.NO_ACCESS
        presenter.onEnrollClick(
            mockedProgramViewModel(),
        )
        verify(view).displayAccessError()
    }

    @Test
    fun `Should show message for no read only access in program`() {
        whenever(
            enrollmentService.blockingGetEnrollmentAccess(
                anyString(),
                anyString(),
            ),
        ) doReturn EnrollmentAccess.READ_ACCESS
        presenter.onEnrollClick(
            mockedProgramViewModel(),
        )
        verify(view).displayAccessError()
    }

    @Test
    fun `Should change current program`() {
        presenter.onActiveEnrollClick(mockedEnrollmentViewModel())
        verify(view).changeCurrentProgram(anyString(), anyString())
    }

    @Test
    fun `Should return program color`() {
        whenever(interactor.getProgramColor(anyString())) doReturn "#ffffff"
        val result = presenter.getProgramColor("programUid")
        assertTrue(result == "#ffffff")
    }

    @Test
    fun `Should unselect enrollment`() {
        presenter.onUnselectEnrollment()
        verify(analyticsHelper).setEvent(anyString(), anyString(), anyString())
        verify(preferenceProvider).removeValue(anyString())
        verify(view).changeCurrentProgram(null, null)
    }

    @Test
    fun `Should detach interactor`() {
        presenter.onDettach()
        verify(interactor).onDettach()
    }

    @Test
    fun `Should displayMessage`() {
        presenter.displayMessage("This is a test message")
        verify(view).displayMessage("This is a test message")
    }

    private fun mockedProgramViewModel(): ProgramViewModel {
        return ProgramViewModel(
            "uid",
            "programName",
            MetadataIconData.Resource(
                programColor = Color.parseColor("#84FFFF"),
                iconResource = R.drawable.ic_home_positive,
            ),
            0,
            "type",
            "typeName",
            "programType",
            null,
            true,
            accessDataWrite = true,
            state = State.SYNCED,
            hasOverdueEvent = false,
            filtersAreActive = false,
            downloadState = ProgramDownloadState.NONE,
            stockConfig = null,
        )
    }

    private fun mockedEnrollmentViewModel(): EnrollmentViewModel {
        return EnrollmentViewModel.create(
            "uid",
            "2020-01-01",
            null,
            "programName",
            "orgUnit",
            false,
            "programUid",
        )
    }
}
