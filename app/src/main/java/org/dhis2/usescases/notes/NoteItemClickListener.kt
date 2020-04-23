package org.dhis2.usescases.notes

import android.view.View
import org.hisp.dhis.android.core.note.Note

interface NoteItemClickListener {
    fun onNoteClick(view: View, note: Note)
}
