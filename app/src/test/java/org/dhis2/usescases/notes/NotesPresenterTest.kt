package org.dhis2.usescases.notes

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import java.util.UUID
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.note.Note
import org.junit.Before
import org.junit.Test

class NotesPresenterTest {

    private lateinit var presenter: NotesPresenter
    private val view: NotesView = mock()
    private val repository: NotesRepository = mock()
    private val noteProcessor: FlowableProcessor<Boolean> = BehaviorProcessor.create()
    private val scheduler = TrampolineSchedulerProvider()
    private var uid: String = UUID.randomUUID().toString()

    @Before
    fun setUpForEnrollment() {
        presenter = NotesPresenter(view, repository, uid, NoteType.ENROLLMENT, scheduler)
    }

    private fun setUpForEvent() {
        presenter = NotesPresenter(view, repository, uid, NoteType.EVENT, scheduler)
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
    fun `Should subscribe to notes for TEI enrollment`() {
        val notes = listOf(dummyNote(), dummyNote())

        whenever(
            repository.getEnrollmentNotes(uid)
        ) doReturn Single.just(notes)
        whenever(
            repository.hasProgramWritePermission(NoteType.ENROLLMENT)
        ) doReturn true

        noteProcessor.onNext(true)
        presenter.subscribeToNotes()

        verify(view).swapNotes(notes)
    }

    @Test
    fun `Should subscribe to notes for events`() {
        setUpForEvent()
        val notes = listOf(dummyNote(), dummyNote())

        whenever(
            repository.getEventNotes(uid)
        ) doReturn Single.just(notes)
        whenever(
            repository.hasProgramWritePermission(NoteType.EVENT)
        ) doReturn false

        noteProcessor.onNext(true)
        presenter.subscribeToNotes()

        verify(view).swapNotes(notes)
    }

    @Test
    fun `Should set no notes layout when notes are empty`() {
        whenever(
            repository.getEnrollmentNotes(uid)
        ) doReturn Single.just(listOf())
        whenever(
            repository.hasProgramWritePermission(NoteType.ENROLLMENT)
        ) doReturn true

        noteProcessor.onNext(true)
        presenter.subscribeToNotes()

        verify(view).setEmptyNotes()
    }

    @Test
    fun `Should set write permission to false`() {
        val notes = listOf(dummyNote(), dummyNote())

        whenever(
            repository.getEnrollmentNotes(uid)
        ) doReturn Single.just(notes)
        whenever(
            repository.hasProgramWritePermission(NoteType.EVENT)
        ) doReturn false

        noteProcessor.onNext(true)
        presenter.subscribeToNotes()

        verify(view).setWritePermission(false)
    }

    private fun dummyNote(): Note =
        Note.builder()
            .uid(UUID.randomUUID().toString())
            .value("Note")
            .build()
}
