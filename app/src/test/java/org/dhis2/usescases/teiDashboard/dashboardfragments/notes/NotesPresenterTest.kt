package org.dhis2.usecases.teiDashboard.dashboardfragments.notes

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.usescases.teiDashboard.DashboardRepositoryImpl
import org.dhis2.usescases.teiDashboard.dashboardfragments.notes.NotesPresenter
import org.dhis2.usescases.teiDashboard.dashboardfragments.notes.NotesView
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.note.Note
import org.hisp.dhis.android.core.note.NoteCreateProjection
import org.hisp.dhis.android.core.program.Program
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

class NotesPresenterTest {
    private lateinit var notesPresenter: NotesPresenter
    private val dashboardRepository: DashboardRepositoryImpl = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val view: NotesView = mock()
    private val d2: D2 = mock()

    @Before
    fun setUp() {
        notesPresenter = NotesPresenter(
            d2, dashboardRepository, schedulers,
            view, "program_uid", "tei_uid"
        )
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        notesPresenter.displayMessage(message)

        verify(view).displayMessage(message)
    }
    @Test
    fun `Should return true if program has write permission`() {
        programModuleMock()
        whenever(
            d2.programModule().programs().uid("program_uid")
                .blockingGet()
        ) doReturn getProgramDefaultAccessTrue()

        assertTrue(notesPresenter.hasProgramWritePermission())
    }

    @Test
    fun `Should return false if program has not write permission`() {
        programModuleMock()
        whenever(
            d2.programModule().programs().uid("program_uid")
                .blockingGet()
        ) doReturn getProgramDefaultAccessFalse()

        assertFalse(notesPresenter.hasProgramWritePermission())
    }

    @Test
    fun `Should return true if program has null write permission`() {
        programModuleMock()
        whenever(
            d2.programModule().programs().uid("program_uid")
                .blockingGet()
        ) doReturn getProgramDefaultAccessNull()

        assertTrue(notesPresenter.hasProgramWritePermission())
    }

    @Test
    fun `Should clear disposables`() {
        notesPresenter.onDettach()

        assertTrue(notesPresenter.compositeDisposable.size() == 0)
    }

    @Test
    fun `Should set note processor`() {
        val dummyPair = Pair.create("test", true)
        val noteProcessor: FlowableProcessor<Pair<String, Boolean>> = BehaviorProcessor.create()
        noteProcessor.onNext(dummyPair)

        notesPresenter.setNoteProcessor(noteProcessor)

        verify(dashboardRepository).handleNote(dummyPair)
    }

    @Test
    fun `Should subscribeToNotes`() {
        val notes = listOf<Note>(Note.builder().uid("note_uid").build())
        val noteProcessor: FlowableProcessor<Boolean> = BehaviorProcessor.create()
        noteProcessor.onNext(true)

        mockEnrollmentByProgramTeiStatus()

        whenever(d2.noteModule()) doReturn mock()
        whenever(d2.noteModule().notes()) doReturn mock()
        whenever(d2.noteModule().notes().byEnrollmentUid()) doReturn mock()
        whenever(d2.noteModule().notes().byEnrollmentUid().eq("enroll_uid")) doReturn mock()
        whenever(
            d2.noteModule().notes().byEnrollmentUid()
                .eq("enroll_uid").get()
        ) doReturn Single.just(notes)

        notesPresenter.subscribeToNotes()

        verify(view).swapNotes(notes)
    }

    @Test
    fun `Should save a note`() {
        val testingNoteUid = "note_uid"

        mockEnrollmentByProgramTeiStatus()

        whenever(d2.noteModule()) doReturn mock()
        whenever(d2.noteModule().notes()) doReturn mock()
        whenever(
            d2.noteModule().notes()
                .blockingAdd(noteCreationProject())
        ) doReturn testingNoteUid

        val testSubscriber = notesPresenter.processor.test()

        notesPresenter.saveNote("message")

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(true)
    }

    private fun getProgramDefaultAccessTrue(): Program {
        return Program.builder()
            .uid("program_uid")
            .access(
                Access.create(
                    false, false,
                    DataAccess.create(false, true)
                )
            )
            .build()
    }

    private fun getProgramDefaultAccessFalse(): Program {
        return Program.builder()
            .uid("program_uid")
            .access(
                Access.create(
                    false, false,
                    DataAccess.create(false, false)
                )
            )
            .build()
    }

    private fun getProgramDefaultAccessNull(): Program {
        return Program.builder()
            .uid("program_uid")
            .access(
                Access.create(
                    false, false,
                    DataAccess.create(false, null)
                )
            )
            .build()
    }

    private fun noteCreationProject(): NoteCreateProjection {
        return NoteCreateProjection.builder()
            .enrollment("enroll_uid")
            .value("message")
            .build()
    }

    private fun programModuleMock() {
        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("program_uid")) doReturn mock()
    }

    private fun mockEnrollmentByProgramTeiStatus() {
        whenever(d2.enrollmentModule()) doReturn mock()
        whenever(d2.enrollmentModule().enrollments()) doReturn mock()
        whenever(d2.enrollmentModule().enrollments().byProgram()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byProgram()
                .eq("program_uid")
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byProgram().eq("program_uid")
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byProgram().eq("program_uid")
                .byTrackedEntityInstance().eq("tei_uid")
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byProgram().eq("program_uid")
                .byTrackedEntityInstance().eq("tei_uid").byStatus()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byProgram().eq("program_uid")
                .byTrackedEntityInstance().eq("tei_uid").byStatus()
                .eq(EnrollmentStatus.ACTIVE)
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byProgram().eq("program_uid")
                .byTrackedEntityInstance().eq("tei_uid").byStatus()
                .eq(EnrollmentStatus.ACTIVE).one()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byProgram().eq("program_uid")
                .byTrackedEntityInstance().eq("tei_uid").byStatus()
                .eq(EnrollmentStatus.ACTIVE).one().blockingGet()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byProgram().eq("program_uid")
                .byTrackedEntityInstance().eq("tei_uid").byStatus()
                .eq(EnrollmentStatus.ACTIVE).one().blockingGet().uid()
        ) doReturn "enroll_uid"
    }
}
