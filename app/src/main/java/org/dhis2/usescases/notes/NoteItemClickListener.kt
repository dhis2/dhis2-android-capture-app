package org.dhis2.usescases.notes

import org.hisp.dhis.android.core.note.Note

interface NoteItemClickListener {
    fun onNoteClick(note: Note)
}