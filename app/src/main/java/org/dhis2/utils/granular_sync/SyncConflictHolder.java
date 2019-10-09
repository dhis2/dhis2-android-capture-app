package org.dhis2.utils.granular_sync;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemSyncConflictBinding;
import org.dhis2.utils.DateUtils;

class SyncConflictHolder extends RecyclerView.ViewHolder {
    private final ItemSyncConflictBinding binding;

    SyncConflictHolder(@NonNull ItemSyncConflictBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(StatusLogItem trackerImportConflict) {
        binding.date.setText(DateUtils.dateTimeFormat().format(trackerImportConflict.date()));
        binding.message.setText(trackerImportConflict.description());
    }
}
