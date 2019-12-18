package org.dhis2.usescases.teiDashboard

import android.os.Bundle
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.program.Program
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
    fun `Should go to enrollment list when clicked`() {
        val extras = Bundle()
        extras.putString("TEI_UID", teiUid)
        presenter.onEnrollmentSelectorClick()-

        verify(view).goToEnrollmentList(extras)
    }

    @Test
    fun `Should set program and restore adapter`() {
        val programUid = "programUid"
        val program = Program.builder().uid(programUid).build()
        presenter.setProgram(program)

        assert(presenter.programUid == programUid)
        verify(view).restoreAdapter(programUid)
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