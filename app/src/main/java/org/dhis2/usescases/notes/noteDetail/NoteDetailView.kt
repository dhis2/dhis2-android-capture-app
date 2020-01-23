package org.dhis2.usescases.notes.noteDetail

/**
 * Created by frodriguez on 1/23/2020.
 *
 */
interface NoteDetailView {
    fun showDiscardDialog()

    fun setNote(note: String)
}