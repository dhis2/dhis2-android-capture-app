package org.dhis2.usescases.notes.noteDetail

import org.dhis2.usescases.notes.NoteType
import org.hisp.dhis.android.core.note.Note

interface NoteDetailView {
    fun showDiscardDialog()

    fun setNote(note: Note)

    fun getNewNote(): Triple<NoteType, String, String>

    fun noteSaved()

    fun back()
}
