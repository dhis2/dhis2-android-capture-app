package org.dhis2.usescases.teiDashboard.dashboardfragments.notes

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.FragmentNotesBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CREATE_NOTE
import org.hisp.dhis.android.core.note.Note

class NotesFragment : FragmentGlobalAbstract(), NotesContracts.View {

    @Inject
    lateinit var presenter: NotesPresenter

    private lateinit var binding: FragmentNotesBinding
    private lateinit var notesAdapter: NotesAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val activity = context as TeiDashboardMobileActivity
        if ((context.applicationContext as App).dashboardComponent() != null) {
            (context.applicationContext as App).dashboardComponent()!!
                .plus(NotesModule(this))
                .inject(this)
            presenter.init(activity.programUid, activity.teiUid)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_notes,
            container, false
        )
        notesAdapter = NotesAdapter()
        binding.notesRecycler.adapter = notesAdapter
        binding.editNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(e: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.buttonDelete.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
        })
        binding.addNoteButton.setOnClickListener(this::addNote)
        binding.buttonDelete.setOnClickListener(this::clearNote)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        presenter.setNoteProcessor(notesAdapter.asFlowable())
        presenter.subscribeToNotes()
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    private fun addNote(view: View) {
        if (presenter.hasProgramWritePermission()) {
            analyticsHelper().setEvent(CREATE_NOTE, CLICK, CREATE_NOTE)
            presenter.saveNote(binding.editNote.text.toString())
            clearNote(view)
        } else {
            displayMessage(getString(R.string.search_access_error))
        }
    }

    private fun clearNote(view: View) = binding.editNote.text!!.clear()

    override fun swapNotes(noteModules: List<Note>) {
        notesAdapter.setItems(noteModules)
    }
}
