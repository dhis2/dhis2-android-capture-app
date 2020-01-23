package org.dhis2.usescases.notes

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.note.Note
import timber.log.Timber

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
class NotesPresenter(
    private val view: NotesContracts.View,
    private val notesRepository: NotesRepository,
    private val teiUid: String?,
    private val eventUid: String?,
    private val schedulerProvider: SchedulerProvider
) {

    private var compositeDisposable = CompositeDisposable()
    private val noteProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()

    fun onDetach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }

    fun subscribeToNotes() {
        compositeDisposable.add(
            noteProcessor.startWith(true)
                .flatMapSingle<List<Note>> {
                    eventUid?.let {
                        notesRepository.getEventNotes(eventUid)
                    } ?: teiUid?.let {
                        notesRepository.getEnrollmentNotes(teiUid)
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view.swapNotes(),
                    Consumer<Throwable> { Timber.d(it) }
                )
        )
    }

    fun hasProgramWritePermission(): Boolean = notesRepository.hasProgramWritePermission()

    fun saveNote(message: String) {
        val addNote = eventUid?.let {
            notesRepository.addEventNote(eventUid, message)
        } ?: notesRepository.addEnrollmentNote(teiUid!!, message)

        compositeDisposable.add(
            addNote
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        noteProcessor.onNext(true)
                    },
                    Timber::e
                )
        )
    }
}
