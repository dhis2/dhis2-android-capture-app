package org.dhis2.usescases.notes.noteDetail

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.notes.NoteType
import org.hisp.dhis.android.core.note.Note
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NoteDetailPresenterTest {

    private lateinit var presenter: NoteDetailPresenter
    private val scheduler = TrampolineSchedulerProvider()
    private val repository: NoteDetailRepository = mock()
    private val view: NoteDetailView = mock()

    @Before
    fun setUp() {
        presenter = NoteDetailPresenter(view, scheduler, "noteUid", repository)
    }

    @Test
    fun `Should set set note`() {
        val note = dummyNote()

        whenever(repository.getNote(any())) doReturn Single.just(note)

        presenter.init()

        verify(view).setNote(note)
    }

    @Test
    fun `Should get message from form and save the note`() {
        val data = Trio.create(NoteType.ENROLLMENT, "enrollmentUid", "Note Message")

        whenever(view.getNewNote()) doReturn data
        whenever(repository.saveNote(any(), any(), any())) doReturn Single.just("uid")

        presenter.save()

        verify(view).getNewNote()
        verify(view).noteSaved()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should perform back action`() {
        presenter.back()

        verify(view).back()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show dialog when pressing clear`() {
        presenter.clear()

        verify(view).showDiscardDialog()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should dispose of the disposables`() {
        presenter.onDetach()

        val result = presenter.disposable.size()

        Assert.assertTrue(result == 0)
    }

    private fun dummyNote() =
        Note.builder().uid("uid").build()
}
