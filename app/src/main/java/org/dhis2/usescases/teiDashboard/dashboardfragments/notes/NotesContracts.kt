package org.dhis2.usescases.teiDashboard.dashboardfragments.notes

import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.note.Note

class NotesContracts {

    interface View : AbstractActivityContracts.View {
        fun swapNotes(noteModules: List<Note>)
    }
}
