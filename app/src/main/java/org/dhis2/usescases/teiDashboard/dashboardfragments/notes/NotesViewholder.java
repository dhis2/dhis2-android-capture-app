package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import android.text.TextUtils;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemNotesBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.enrollment.note.Note;

import java.text.ParseException;
import java.util.Date;

/**
 * QUADRAM. Created by Administrador on 18/12/2017.
 */

public class NotesViewholder extends RecyclerView.ViewHolder {

    private final ItemNotesBinding binding;

    public NotesViewholder(ItemNotesBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Note note) {
        if (note.storedDate() != null) {
            String formattedDate;
            try {
                Date date = DateUtils.databaseDateFormat().parse(note.storedDate());
                formattedDate = DateUtils.uiDateFormat().format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                formattedDate = note.storedDate();
            }

            binding.date.setText(formattedDate);
        }
        binding.noteText.setText(note.value());
        binding.storeBy.setText(note.storedBy());
        if (note.storedBy() != null) {
            String letterA = note.storedBy().split(" ")[0].substring(0, 1);
            String letterB = note.storedBy().split(" ").length > 1 ? note.storedBy().split(" ")[1].substring(0, 1) : "";
            binding.userInit.setText(String.format("%s%s", letterA, letterB));
        }
        binding.executePendingBindings();
        itemView.setOnClickListener(view -> {
            if (binding.noteText.getMaxLines() == 1) {
                binding.noteText.setMaxLines(Integer.MAX_VALUE);
                binding.noteText.setEllipsize(null);
            } else {
                binding.noteText.setMaxLines(1);
                binding.noteText.setEllipsize(TextUtils.TruncateAt.END);
            }
        });
    }
}
