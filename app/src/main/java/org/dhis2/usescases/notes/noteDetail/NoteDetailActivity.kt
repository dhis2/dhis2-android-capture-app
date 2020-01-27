package org.dhis2.usescases.notes.noteDetail

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.ActivityNoteDetailBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.note.Note
import javax.inject.Inject

class NoteDetailActivity : ActivityGlobalAbstract(), NoteDetailView {

    private lateinit var binding: ActivityNoteDetailBinding

    @Inject
    lateinit var presenter: NoteDetailPresenter

    private val isNewNote: ObservableBoolean = ObservableBoolean(true)
    private lateinit var noteType: NoteType
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId: String? = intent.getStringExtra(Constants.NOTE_ID)
        val programUid = intent.getStringExtra(Constants.PROGRAM_UID)
        noteType = intent.getSerializableExtra(Constants.NOTE_TYPE) as NoteType
        uid = intent.getStringExtra(Constants.UID)

        app().userComponent()?.plus(NoteDetailModule(this, noteId, programUid))?.inject(this)
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
        val dialog = MaterialAlertDialogBuilder(this)
            .setMessage(R.string.discard_note)
            .setPositiveButton(R.string.yes) { _, _ ->
                setResult(RESULT_CANCELED)
                finish()
            }
            .setNegativeButton(R.string.no, null)
        dialog.show()
    }

    override fun setNote(note: Note) {
        // TODO: Change to correct note values
        binding.userName.text = note.storedBy()
        binding.noteTime.text = note.storedDate() // TODO: needs to be formatted
        binding.note.text = note.value()
    }

    override fun getNewNote(): Trio<NoteType, String, String> {
        return Trio.create(noteType, uid, binding.noteText.text.toString())
    }

    override fun noteSaved() {
        showToast(getString(R.string.note_saved))
        setResult(RESULT_OK)
        finish()
    }

    override fun back() {
        if (isNewNote.get()) {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    override fun onBackPressed() {
        back()
    }

}
