package org.dhis2.usescases.notes.noteDetail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.dhis2.Bindings.app
import org.dhis2.Bindings.initials
import org.dhis2.Bindings.placeHolder
import org.dhis2.R
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.ActivityNoteDetailBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.notes.NoteType
import org.dhis2.utils.Constants
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.note.Note
import java.text.ParseException
import javax.inject.Inject

class NoteDetailActivity : ActivityGlobalAbstract(), NoteDetailView, TextWatcher {

    private lateinit var binding: ActivityNoteDetailBinding

    @Inject
    lateinit var presenter: NoteDetailPresenter

    private val isNewNote: ObservableBoolean = ObservableBoolean(true)
    private val showButtons: ObservableBoolean = ObservableBoolean(false)
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
            showButtons = this@NoteDetailActivity.showButtons
            presenter = this@NoteDetailActivity.presenter
        }

        if (!isNewNote.get()) {
            presenter.init()
        } else {
            binding.noteText.placeHolder(getString(R.string.write_new_note))
            binding.noteText.addTextChangedListener(this)
        }
    }

    override fun onPause() {
        presenter.onDetach()
        super.onPause()
    }

    override fun showDiscardDialog() {
        val dialog = MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
            .setMessage(R.string.discard_note)
            .setPositiveButton(R.string.yes) { _, _ ->
                finish()
            }
            .setNegativeButton(R.string.no, null)
        dialog.show()
    }

    override fun setNote(note: Note) {
        binding.userInit.text = note.storedBy().initials
        binding.storeBy.text = note.storedBy()
        binding.note.text = note.value()
        note.storedDate()?.let {
            val formattedDate = try {
                val date = DateUtils.databaseDateFormat().parse(note.storedDate())
                DateUtils.uiDateFormat().format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
                note.storedDate()
            }
            binding.noteTime.text = formattedDate
        }
        binding.executePendingBindings()
    }

    override fun getNewNote(): Trio<NoteType, String, String> {
        return Trio.create(noteType, uid, binding.noteText.text.toString())
    }

    override fun noteSaved() {
        showToast(getString(R.string.note_saved))
        finish()
    }

    override fun back() {
        if (isNewNote.get() && binding.noteText.text.toString().isNotEmpty() ) {
            showDiscardDialog()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        back()
    }

    override fun afterTextChanged(editable: Editable?) {
        if(!editable.toString().isNullOrEmpty()){
            showButtons.set(true)
        }
    }

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
}
