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
package org.dhis2.usescases.notes

import android.view.View
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.note.Note
import timber.log.Timber

class NotesPresenter(
    private val view: NotesView,
    private val notesRepository: NotesRepository,
    private val uid: String,
    private val noteType: NoteType,
    private val schedulerProvider: SchedulerProvider
) {

    var compositeDisposable = CompositeDisposable()
    val noteProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()

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
                    when (noteType) {
                        NoteType.EVENT -> notesRepository.getEventNotes(uid)
                        NoteType.ENROLLMENT -> notesRepository.getEnrollmentNotes(uid)
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    this::handleNotes,
                    Timber::e
                )
        )

        compositeDisposable.add(
            Flowable.just(notesRepository.hasProgramWritePermission())
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        view.setWritePermission(it)
                    },
                    Timber::e
                )
        )
    }

    private fun handleNotes(notes: List<Note>) {
        when {
            notes.isEmpty() -> view.setEmptyNotes()
            else -> view.swapNotes(notes)
        }
    }
}
