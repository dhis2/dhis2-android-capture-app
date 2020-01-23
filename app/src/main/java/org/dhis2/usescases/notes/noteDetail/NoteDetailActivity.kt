package org.dhis2.usescases.notes.noteDetail

import android.os.Bundle
import org.dhis2.R
import org.dhis2.usescases.general.ActivityGlobalAbstract

class NoteDetailActivity : ActivityGlobalAbstract(), NoteDetailView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)
    }

    override fun showDiscardDialog() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setNote(note: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
