package org.dhis2.usescases.notes.noteDetail

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.databinding.ActivityNoteDetailBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import javax.inject.Inject

class NoteDetailActivity : ActivityGlobalAbstract(), NoteDetailView {

    private lateinit var binding: ActivityNoteDetailBinding

    @Inject
    lateinit var presenter: NoteDetailPresenter

    private val isNewNote: ObservableBoolean = ObservableBoolean(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId: String? = intent.getStringExtra("NOTE_ID")
        app().userComponent()?.plus(NoteDetailModule(this, noteId))?.inject(this)
        noteId?.let { isNewNote.set(false) }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note_detail)
        binding.apply {
            isForm = isNewNote
            presenter = presenter
        }

        if (!isNewNote.get()) {
            presenter.init()
        }
    }

    override fun showDiscardDialog() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setNote(note: String) {
        // TODO: Change to correct note values
        binding.userName.text = "User Name"
        binding.noteTime.text = "1 min ago"
        binding.note.text = note
    }

    override fun getNoteMessage(): String {
        return binding.noteText.text.toString()
    }
}
