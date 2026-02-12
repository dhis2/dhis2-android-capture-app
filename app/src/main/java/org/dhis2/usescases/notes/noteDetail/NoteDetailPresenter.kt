package org.dhis2.usescases.notes.noteDetail

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.usescases.notes.NotesIdlingResource
import timber.log.Timber

class NoteDetailPresenter(
    private val view: NoteDetailView,
    private val scheduler: SchedulerProvider,
    private val noteId: String?,
    private val repository: NoteDetailRepository,
) {
    val disposable = CompositeDisposable()

    fun init() {
        disposable.add(
            repository
                .getNote(noteId!!)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe(
                    { note -> note?.let { view.setNote(note) } },
                    Timber::d,
                ),
        )
    }

    fun save() {
        val data = view.getNewNote()
        val noteType = data.first
        val uid = data.second
        val message = data.third
        NotesIdlingResource.increment()
        disposable.add(
            repository
                .saveNote(noteType, uid, message)
                .doOnSuccess { NotesIdlingResource.decrement() }
                .doOnError { NotesIdlingResource.decrement() }
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe(
                    { view.noteSaved() },
                    Timber::d,
                ),
        )
    }

    fun back() {
        view.back()
    }

    fun clear() {
        view.showDiscardDialog()
    }

    fun onDetach() {
        disposable.clear()
    }
}
