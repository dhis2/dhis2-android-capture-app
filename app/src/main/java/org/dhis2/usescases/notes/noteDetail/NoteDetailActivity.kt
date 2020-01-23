package org.dhis2.usescases.notes.noteDetail

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlinx.android.synthetic.main.activity_add_note.view.noteText
import kotlinx.android.synthetic.main.activity_login.view.userIcon
import kotlinx.android.synthetic.main.activity_login.view.user_name
import kotlinx.android.synthetic.main.activity_note_detail.view.note
import kotlinx.android.synthetic.main.activity_note_detail.view.userName
import kotlinx.android.synthetic.main.activity_note_detail.view.user_image
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.usescases.general.ActivityGlobalAbstract
import javax.inject.Inject

class NoteDetailActivity : ActivityGlobalAbstract(), NoteDetailView {

    private lateinit var binding: ViewDataBinding

    @Inject
    lateinit var presenter: NoteDetailPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId: String? = intent.getStringExtra("NOTES_ID")
        app().userComponent()?.plus(NoteDetailModule(this, noteId))?.inject(this)
        binding = if (noteId.isNullOrEmpty()) {
            DataBindingUtil.setContentView(this, R.layout.activity_add_note)
        } else {
            DataBindingUtil.setContentView(this, R.layout.activity_note_detail)
        }
    }

    override fun showDiscardDialog() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setNote(note: String) {
        // TODO: Change to correct note values
        binding.root.userName.text = "User Name"
        binding.root.note.text = note
    }

    override fun getNoteMessage(): String {
        return binding.root.noteText.text.toString()
    }
}
