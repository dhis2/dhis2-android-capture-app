package org.dhis2.data.forms.dataentry.fields.edittext;


import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;


public class EditTextCustomHolder extends FormViewHolder {

    public EditTextCustomHolder(ViewDataBinding binding, MutableLiveData<String> currentSelection) {
        super(binding);
        this.currentUid = currentSelection;
    }

    @Override
    public void update(@NonNull FieldViewModel model) {
    }
}
