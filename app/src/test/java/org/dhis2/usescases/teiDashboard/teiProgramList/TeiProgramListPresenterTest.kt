package org.dhis2.usescases.teiDashboard.teiProgramList

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.usescases.main.program.ProgramViewModel
import org.dhis2.utils.analytics.AnalyticsHelper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

class TeiProgramListPresenterTest {

    lateinit var presenter: TeiProgramListPresenter
    private val view: TeiProgramListContract.View = mock()
    private val interactor: TeiProgramListContract.Interactor = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val dhisEnrollmentUtils: DhisEnrollmentUtils = mock()

    @Before
    fun setUp() {
        presenter = TeiProgramListPresenter(
            view,
            interactor,
            "teiUid",
            preferenceProvider,
            analyticsHelper,
            dhisEnrollmentUtils
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
    fun `Should enroll in open program`() {
        whenever(
            dhisEnrollmentUtils.canCreateEnrollmentInProtectedProgram(
                anyString(),
                anyString()
            )
        ) doReturn DhisEnrollmentUtils.CreateEnrollmentStatus.OPEN_PROGRAM_OK
        presenter.onEnrollClick(
            mockedProgramViewModel()
        )
        verify(analyticsHelper).setEvent(anyString(), anyString(), anyString())
        verify(preferenceProvider).removeValue(anyString())
        verify(interactor).enroll(anyString(), anyString())
    }

    @Test
    fun `Should enroll in protected program`() {
        whenever(
            dhisEnrollmentUtils.canCreateEnrollmentInProtectedProgram(
                anyString(),
                anyString()
            )
        ) doReturn DhisEnrollmentUtils.CreateEnrollmentStatus.PROTECTED_PROGRAM_OK
        presenter.onEnrollClick(
            mockedProgramViewModel()
        )
        verify(analyticsHelper).setEvent(anyString(), anyString(), anyString())
        verify(preferenceProvider).removeValue(anyString())
        verify(interactor).enroll(anyString(), anyString())
    }

    @Test
    fun `Should show message for protected program`() {
        whenever(
            dhisEnrollmentUtils.canCreateEnrollmentInProtectedProgram(
                anyString(),
                anyString()
            )
        ) doReturn DhisEnrollmentUtils.CreateEnrollmentStatus.PROTECTED_PROGRAM_DENIED
        presenter.onEnrollClick(
            mockedProgramViewModel()
        )
        verify(view).displayBreakGlassError()
    }

    @Test
    fun `Should show message for no access in program`() {
        whenever(
            dhisEnrollmentUtils.canCreateEnrollmentInProtectedProgram(
                anyString(),
                anyString()
            )
        ) doReturn DhisEnrollmentUtils.CreateEnrollmentStatus.PROGRAM_ACCESS_DENIED
        presenter.onEnrollClick(
            mockedProgramViewModel()
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
        return ProgramViewModel.create(
            "uid",
            "programName",
            null,
            null,
            0,
            "type",
            "typeName",
            "programType",
            null,
            true,
            accessDataWrite = true,
            state = "Open"
        )
    }

    private fun mockedEnrollmentViewModel(): EnrollmentViewModel {
        return EnrollmentViewModel.create(
            "uid",
            "2020-01-01",
            null,
            null,
            "programName",
            "orgUnit",
            false,
            "programUid"
        )
    }
}
