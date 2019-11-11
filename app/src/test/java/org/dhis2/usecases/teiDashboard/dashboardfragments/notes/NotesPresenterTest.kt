package org.dhis2.usecases.teiDashboard.dashboardfragments.notes

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.teiDashboard.DashboardRepositoryImpl
import org.dhis2.usescases.teiDashboard.dashboardfragments.notes.NotesContracts
import org.dhis2.usescases.teiDashboard.dashboardfragments.notes.NotesPresenterImpl
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.program.Program
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotesPresenterTest {
    private lateinit var notesPresenter: NotesPresenterImpl
    private val dashboardRepository: DashboardRepositoryImpl = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val view: NotesContracts.View = mock()
    private val d2: D2 = mock ()

    @Before
    fun setUp(){
        notesPresenter = NotesPresenterImpl(d2, dashboardRepository, schedulers, view)

    }

    @Test
    fun `Should init successfully and show reserved values`(){
        notesPresenter.init("program_uid", "tei_uid")
    }
    @Test
    fun `Should display message`(){
        val message = "message"

        notesPresenter.displayMessage(message)

        verify(view).displayMessage(message)
    }
    @Test
    fun `Should return if program has write permission`(){
        notesPresenter.init("program_uid", "tei_uid")

        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("program_uid")) doReturn mock()
        whenever(d2.programModule().programs().uid("program_uid")
                .blockingGet()) doReturn getProgramDefault()

        assertTrue(notesPresenter.hasProgramWritePermission())
    }

    @Test
    fun `Should clear disposables`(){
        notesPresenter.onDettach()

        assertTrue(notesPresenter.compositeDisposable.size() == 0)
    }
    @Test
    fun `Should handle notes`(){

    }

    private fun getProgramDefault(): Program{
        return Program.builder()
                .uid("program_uid")
                .access(Access.create(false, false,
                DataAccess.create(false, true)))
                .build()
    }
}