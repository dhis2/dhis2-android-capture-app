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

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.Bindings.initials
import org.dhis2.Bindings.setDateInterval
import org.dhis2.databinding.ItemNoteBinding
import org.hisp.dhis.android.core.note.Note

class NotesViewHolder(private val binding: ItemNoteBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note, listener: NoteItemClickListener) {
        note.storedDate()?.let {
            binding.date.setDateInterval(it)
/*
            val formattedDate = try {
                val date = DateUtils.databaseDateFormat().parse(note.storedDate())
                DateUtils.uiDateFormat().format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
                note.storedDate()
            }
            binding.date.text = formattedDate*/
        }

        binding.noteText.text = note.value()
        val storedBy = "@${note.storedBy()}"
        binding.storeBy.text = storedBy
        binding.userInit.text = note.storedBy().initials
        itemView.setOnClickListener { listener.onNoteClick(itemView, note) }
        binding.executePendingBindings()
    }
}
