package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.picture.PictureViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;

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
        if(oldFields.get(oldItemPosition) instanceof PictureViewModel && newFields.get(newItemPosition) instanceof PictureViewModel)
            return false;

        return oldFields.get(oldItemPosition) == newFields.get(newItemPosition);
       /* if(oldFields.get(oldItemPosition) instanceof SpinnerViewModel && newFields.get(newItemPosition) instanceof SpinnerViewModel){
            if(!((SpinnerViewModel) oldFields.get(oldItemPosition)).getOptionGroupsToShow().containsAll(
                    ((SpinnerViewModel) newFields.get(newItemPosition)).getOptionGroupsToShow()) ||
                !((SpinnerViewModel) newFields.get(oldItemPosition)).getOptionGroupsToShow().containsAll(
                    ((SpinnerViewModel) oldFields.get(newItemPosition)).getOptionGroupsToShow()))
                return false;

            if(!((SpinnerViewModel) oldFields.get(oldItemPosition)).getOptionGroupsToHide().containsAll(
                    ((SpinnerViewModel) newFields.get(newItemPosition)).getOptionGroupsToHide()) ||
                    !((SpinnerViewModel) newFields.get(oldItemPosition)).getOptionGroupsToHide().containsAll(
                            ((SpinnerViewModel) oldFields.get(newItemPosition)).getOptionGroupsToHide()))
                return false;

            if(!((SpinnerViewModel) oldFields.get(oldItemPosition)).getOptionsToHide().containsAll(
                    ((SpinnerViewModel) newFields.get(newItemPosition)).getOptionsToHide()) ||
                    !((SpinnerViewModel) newFields.get(oldItemPosition)).getOptionsToHide().containsAll(
                            ((SpinnerViewModel) oldFields.get(newItemPosition)).getOptionsToHide()))
                return false;
        }

        if(oldFields.get(oldItemPosition) instanceof PictureViewModel && newFields.get(newItemPosition) instanceof PictureViewModel)
            return false;

        return oldFields.get(oldItemPosition)
                .equals(newFields.get(newItemPosition));*/
    }
}
