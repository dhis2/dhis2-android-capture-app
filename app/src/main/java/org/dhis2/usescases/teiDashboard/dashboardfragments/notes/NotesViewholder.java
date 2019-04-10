package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;

import org.dhis2.databinding.ItemNotesBinding;
import org.dhis2.utils.DateUtils;

import org.hisp.dhis.android.core.enrollment.note.NoteModel;

/**
 * QUADRAM. Created by Administrador on 18/12/2017.
 */

public class NotesViewholder extends RecyclerView.ViewHolder {

    private final ItemNotesBinding binding;

    public NotesViewholder(ItemNotesBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(NoteModel note) {
        if (note.storedDate() != null) {
            binding.date.setText(DateUtils.uiDateFormat().format(note.storedDate()));
        }
        binding.noteText.setText(note.value());
        binding.storeBy.setText(note.storedBy());
        binding.executePendingBindings();
        itemView.setOnClickListener(view->{
            if(binding.noteText.getMaxLines() == 1) {
                binding.noteText.setMaxLines(Integer.MAX_VALUE);
                binding.noteText.setEllipsize(null);
            }else{
                binding.noteText.setMaxLines(1);
                binding.noteText.setEllipsize(TextUtils.TruncateAt.END);
            }
        });
    }
}
