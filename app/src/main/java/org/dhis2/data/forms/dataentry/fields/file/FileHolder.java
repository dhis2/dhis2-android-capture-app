package org.dhis2.data.forms.dataentry.fields.file;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class FileHolder extends FormViewHolder {

    public FileHolder(ViewDataBinding binding, MutableLiveData<String> currentSelection) {
        super(binding);
        currentUid = currentSelection;
    }

    @Override
    protected void update(FieldViewModel viewModel) {
    }
}
