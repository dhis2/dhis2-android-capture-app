package org.dhis2.usescases.notes

import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.note.Note

interface NotesView : AbstractActivityContracts.View {
    fun swapNotes(noteModules: List<Note>)
}
