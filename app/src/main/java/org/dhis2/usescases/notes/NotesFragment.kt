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
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.FragmentNotesBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.notes.noteDetail.NoteDetailActivity
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.note.Note

class NotesFragment : FragmentGlobalAbstract(), NotesView, NoteItemClickListener {

    @Inject
    lateinit var presenter: NotesPresenter

    private lateinit var binding: FragmentNotesBinding
    private lateinit var noteAdapter: NotesAdapter

    private var programUid: String? = null
    private lateinit var uid: String
    private lateinit var noteType: NoteType

    companion object {
        @JvmStatic
        fun newEventInstance(programUid: String, eventUid: String): NotesFragment {
            val instance = NotesFragment()
            val args = Bundle()
            args.putString(Constants.PROGRAM_UID, programUid)
            args.putString(Constants.UID, eventUid)
            args.putSerializable(Constants.NOTE_TYPE, NoteType.EVENT)
            instance.arguments = args
            return instance
        }

        @JvmStatic
        fun newTrackerInstance(programUid: String, teiUid: String): NotesFragment {
            val instance = NotesFragment()
            val args = Bundle()
            args.putString(Constants.PROGRAM_UID, programUid)
            args.putString(Constants.UID, teiUid)
            args.putSerializable(Constants.NOTE_TYPE, NoteType.ENROLLMENT)
            instance.arguments = args
            return instance
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        programUid = arguments?.getString(Constants.PROGRAM_UID)
        uid = arguments?.getString(Constants.UID) as String
        noteType = arguments?.getSerializable(Constants.NOTE_TYPE) as NoteType
        (context.applicationContext as App)
            .userComponent()!!
            .plus(NotesModule(this, programUid!!, uid, noteType))
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notes, container, false)
        noteAdapter = NotesAdapter(this)
        binding.notesRecycler.adapter = noteAdapter
        binding.addNoteButton.setOnClickListener {
            val intent = Intent(activity, NoteDetailActivity::class.java).apply {
                putExtra(Constants.PROGRAM_UID, programUid)
                putExtra(Constants.UID, uid)
                putExtra(Constants.NOTE_TYPE, noteType)
            }
            startActivity(intent)
        }
        return binding.root
    }

    override fun onNoteClick(view: View, note: Note) {
        val intent = Intent(activity, NoteDetailActivity::class.java).apply {
            putExtra(Constants.NOTE_ID, note.uid())
            putExtra(Constants.PROGRAM_UID, programUid)
            putExtra(Constants.UID, uid)
            putExtra(Constants.NOTE_TYPE, noteType)
        }
        val pairStoredBy = Pair.create<View, String>(
            view.findViewById<TextView>(R.id.storeBy), getString(R.string.transitionElement_storeBy)
        )
        val pairNoteText =
            Pair.create<View, String>(
                view.findViewById<TextView>(R.id.note_text),
                getString(R.string.transitionElement_note_text)
            )
        val pairUserImage =
            Pair.create<View, String>(
                view.findViewById<ImageView>(R.id.userImage),
                getString(R.string.transitionElement_userImage)
            )
        val pairUserInit =
            Pair.create<View, String>(
                view.findViewById<ImageView>(R.id.userInit),
                getString(R.string.transitionElement_userInit)
            )
        val pairDate = Pair.create<View, String>(
            view.findViewById<ImageView>(R.id.date), getString(R.string.transitionElement_date)
        )
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            abstractActivity,
            pairNoteText,
            pairStoredBy,
            pairUserImage,
            pairUserInit,
            pairDate
        )
        startActivity(intent, options.toBundle())
    }

    override fun onResume() {
        super.onResume()
        presenter.subscribeToNotes()
    }

    override fun onPause() {
        presenter.onDetach()
        super.onPause()
    }

    override fun swapNotes(notes: List<Note>) {
        binding.noNotesLayout.visibility = View.GONE
        binding.notesRecycler.visibility = View.VISIBLE
        noteAdapter.setItems(notes)
    }

    override fun setEmptyNotes() {
        binding.noNotesLayout.visibility = View.VISIBLE
        binding.notesRecycler.visibility = View.GONE
    }

    override fun setWritePermission(writePermission: Boolean) {
        if (!writePermission) {
            binding.noPermissionLayout.visibility = View.VISIBLE
            binding.addNoteButton.visibility = View.GONE
        }
    }
}
