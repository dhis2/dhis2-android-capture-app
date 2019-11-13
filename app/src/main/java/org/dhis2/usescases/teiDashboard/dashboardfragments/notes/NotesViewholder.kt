package org.dhis2.usescases.teiDashboard.dashboardfragments.notes

import android.text.TextUtils
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.databinding.ItemNotesBinding
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.note.Note

class NotesViewholder(val binding: ItemNotesBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note) {
        if (note.storedDate() != null) {
            binding.date.text = try {
                val date = DateUtils.databaseDateFormat()
                    .parse(note.storedDate())
                DateUtils.uiDateFormat().format(date)
            } catch (e: Exception) {
                e.printStackTrace()
                note.storedDate()!!
            }
        }

        binding.noteText.text = note.value()
        binding.storeBy.text = note.storedBy()
        if (note.storedBy() != null) {
            val letterA = note.storedBy()!!
                .split(" ")[0].substring(0, 1)
            val letterB = if (note.storedBy()!!.split(" ").size > 1) {
                note.storedBy()!!.split(" ")[1].substring(0, 1)
            } else { "" }

            binding.userInit.text = String.format("%s%s", letterA, letterB)
        }

        binding.executePendingBindings()

        itemView.setOnClickListener {
            if (binding.noteText.maxLines == 1) {
                binding.noteText.maxLines = Integer.MAX_VALUE
                binding.noteText.ellipsize = null
            } else {
                binding.noteText.maxLines = 1
                binding.noteText.ellipsize = TextUtils.TruncateAt.END
            }
        }
    }
}
