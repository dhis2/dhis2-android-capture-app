package org.dhis2.usescases.notes.noteDetail

import android.os.Bundle
import org.dhis2.R
import org.dhis2.usescases.general.ActivityGlobalAbstract

class NoteDetailActivity : ActivityGlobalAbstract() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)
    }
}
