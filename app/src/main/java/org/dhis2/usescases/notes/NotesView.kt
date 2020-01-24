package org.dhis2.usescases.notes

import io.reactivex.functions.Consumer
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.note.Note


interface NotesView : AbstractActivityContracts.View {
    fun swapNotes(): Consumer<List<Note>>
}
