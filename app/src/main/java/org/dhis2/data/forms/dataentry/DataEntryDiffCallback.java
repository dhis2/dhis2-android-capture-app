package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;

import java.util.List;

final class DataEntryDiffCallback extends DiffUtil.Callback {

    @NonNull
    private final List<FieldViewModel> oldFields;

    @NonNull
    private final List<FieldViewModel> newFields;

    DataEntryDiffCallback(@NonNull List<FieldViewModel> oldFields,
                          @NonNull List<FieldViewModel> newFields) {
        this.oldFields = oldFields;
        this.newFields = newFields;
    }

    @Override
    public int getOldListSize() {
        return oldFields.size();
    }

    @Override
    public int getNewListSize() {
        return newFields.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldFields.get(oldItemPosition).uid()
                .equals(newFields.get(newItemPosition).uid());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldFields == newFields;
    }
}
