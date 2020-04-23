package org.dhis2.usescases.notes.noteDetail

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.schedulers.SchedulerProvider
import timber.log.Timber

class NoteDetailPresenter(
    private val view: NoteDetailView,
    private val scheduler: SchedulerProvider,
    private val noteId: String?,
    private val repository: NoteDetailRepository
) {

    val disposable = CompositeDisposable()

    fun init() {
        disposable.add(
            repository.getNote(noteId!!)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe(
                    view::setNote,
                    Timber::d
                )
        )
    }

    fun save() {
        val data = view.getNewNote()
        val noteType = data.val0() ?: throw IllegalArgumentException("")
        val uid = data.val1()!!
        val message = data.val2()!!
        disposable.add(
            repository.saveNote(noteType, uid, message)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe(
                    { view.noteSaved() },
                    Timber::d
                )
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
