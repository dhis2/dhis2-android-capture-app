package org.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import org.dhis2.databinding.ItemNotesBinding;
import org.dhis2.utils.DateUtils;

import org.hisp.dhis.android.core.enrollment.note.NoteModel;

/**
 * Created by Administrador on 18/12/2017.
 */

public class NotesViewholder extends RecyclerView.ViewHolder {

    private final ItemNotesBinding binding;

    public NotesViewholder(ItemNotesBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(NoteModel note) {
        binding.date.setText(DateUtils.uiDateFormat().format(note.storedDate()));
        binding.noteText.setText(note.value());
        binding.storeBy.setText(note.storedBy());
        binding.executePendingBindings();
    }
}
