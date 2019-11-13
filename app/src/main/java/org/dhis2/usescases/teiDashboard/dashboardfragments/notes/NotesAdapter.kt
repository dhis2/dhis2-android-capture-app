package org.dhis2.usescases.teiDashboard.dashboardfragments.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.R
import org.dhis2.data.tuples.Pair
import org.dhis2.databinding.ItemNotesBinding
import org.hisp.dhis.android.core.note.Note

class NotesAdapter : RecyclerView.Adapter<NotesViewholder>() {

    private var notes: List<Note> = arrayListOf()
    private final var processor: FlowableProcessor<Pair<String, Boolean>> =
        PublishProcessor.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewholder {
        val itemNotesBinding =
            DataBindingUtil.inflate<ItemNotesBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_notes, parent, false
            )
        return NotesViewholder(itemNotesBinding)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: NotesViewholder, position: Int) {
        holder.bind(notes[position])
    }

    fun setItems(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }

    fun asFlowable(): FlowableProcessor<Pair<String, Boolean>> = processor
}
