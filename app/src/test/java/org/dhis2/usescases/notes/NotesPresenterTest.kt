package org.dhis2.usescases.notes

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.FlowableProcessor
import io.reactivex.schedulers.TestScheduler
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.note.Note
import org.hisp.dhis.android.core.note.NoteCreateProjection
import org.hisp.dhis.android.core.program.Program
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.UUID

class NotesPresenterTest {

    private lateinit var presenter: NotesPresenter
    private val view: NotesView = mock()
    private val repository: NotesRepository = mock()
    private val noteProcessor: FlowableProcessor<Boolean> = mock()
    private val scheduler = TestSchedulerProvider(TestScheduler())
    private var eventUid = UUID.randomUUID().toString()
    private var teiUid = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        presenter = NotesPresenter(view, repository, eventUid, teiUid, scheduler)
    }

    @Test
    fun `Should dispose of all disposables`() {
        presenter.onDetach()

        val result = presenter.compositeDisposable.size()

        assert(result == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }

    @Test
    fun `Should subscribe to notes for events`() {
        val notes = listOf(dummyNote(), dummyNote())
        teiUid = null.toString()

        whenever(
            noteProcessor.startWith(true)
        ) doReturn Flowable.just(true)
        whenever(
            repository.getEventNotes(eventUid)
        ) doReturn Single.just(notes)

        presenter.subscribeToNotes()

        verify(view).swapNotes()
    }

    @Test
    fun `Should subscribe to notes for TEI enrollment`() {
        val notes = listOf(dummyNote(), dummyNote())
        eventUid = null.toString()

        whenever(
            noteProcessor.startWith(true)
        ) doReturn Flowable.just(true)
        whenever(
            repository.getEnrollmentNotes(teiUid)
        ) doReturn Single.just(notes)

        presenter.subscribeToNotes()

        verify(view).swapNotes()
    }

    @Test
    fun `Should check program write permission`() {
        whenever(
            repository.hasProgramWritePermission()
        ) doReturn true

        assert(presenter.hasProgramWritePermission())
    }

    @Test
    fun `Should save note to event`() {
        teiUid = null.toString()
        val message = "note"
        val newNoteUID = UUID.randomUUID().toString()

        whenever(
            repository.addEventNote(eventUid, message)
        ) doReturn Single.just(newNoteUID)

       verify(noteProcessor).onNext(true)
    }

    @Test
    fun `Should save note to TEI enrollment`() {
        eventUid = null.toString()
        val message = "note"
        val newNoteUID = UUID.randomUUID().toString()

        whenever(
            repository.addEnrollmentNote(teiUid, message)
        ) doReturn Single.just(newNoteUID)

        verify(noteProcessor).onNext(true)
    }

    private fun dummyNote(): Note =
        Note.builder()
            .uid(UUID.randomUUID().toString())
            .value("Note")
            .build()
}