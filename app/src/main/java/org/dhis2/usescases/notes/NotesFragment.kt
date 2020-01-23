/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dhis2.usescases.notes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import io.reactivex.functions.Consumer
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.FragmentNotesBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.Constants
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CREATE_NOTE
import org.hisp.dhis.android.core.note.Note

class NotesFragment : FragmentGlobalAbstract(), NotesContracts.View {

    @Inject
    lateinit var presenter: NotesPresenter

    private lateinit var binding: FragmentNotesBinding
    private lateinit var noteAdapter: NotesAdapter

    private var programUid: String? = null
    private var eventUid: String? = null
    private var teiUid: String? = null

    companion object {
        @JvmStatic
        fun newEventInstance(programUid: String, eventUid: String): NotesFragment {
            val instance = NotesFragment()
            val args = Bundle()
            args.putString(Constants.PROGRAM_UID, programUid)
            args.putString(Constants.EVENT_UID, eventUid)
            instance.arguments = args
            return instance
        }

        @JvmStatic
        fun newTrackerInstance(programUid: String, teiUid: String): NotesFragment {
            val instance = NotesFragment()
            val args = Bundle()
            args.putString(Constants.PROGRAM_UID, programUid)
            args.putString(Constants.TRACKED_ENTITY_UID, teiUid)
            instance.arguments = args
            return instance
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        programUid = arguments?.getString(Constants.PROGRAM_UID)
        eventUid = arguments?.getString(Constants.EVENT_UID)
        teiUid = arguments?.getString(Constants.TRACKED_ENTITY_UID)
        (context.applicationContext as App)
            .userComponent()!!
            .plus(NotesModule(this, programUid!!, teiUid, eventUid))
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notes, container, false)
        noteAdapter = NotesAdapter()
        binding.notesRecycler.adapter = noteAdapter
        binding.addNoteButton.setOnClickListener { this.addNote(it) }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        presenter.subscribeToNotes()
    }

    override fun onPause() {
        presenter.onDetach()
        super.onPause()
    }

    fun addNote(view: View) {
        if (presenter.hasProgramWritePermission()) {
            analyticsHelper().setEvent(CREATE_NOTE, CLICK, CREATE_NOTE)
            // presenter.saveNote(binding.editNote.text!!.toString())
            clearNote(view)
        } else {
            displayMessage(getString(R.string.search_access_error))
        }
    }

    fun clearNote(view: View) {
        // binding.editNote.text!!.clear()
    }

    override fun swapNotes(): Consumer<List<Note>> {
        return Consumer { noteModels -> noteAdapter.setItems(noteModels) }
    }
}
