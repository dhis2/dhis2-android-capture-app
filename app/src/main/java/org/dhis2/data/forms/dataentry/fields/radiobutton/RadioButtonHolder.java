package org.dhis2.data.forms.dataentry.fields.radiobutton;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018.
 */

public class RadioButtonHolder extends FormViewHolder {

    public RadioButtonHolder(ViewDataBinding binding, MutableLiveData<String> currentSelection) {
        super(binding);
        currentUid = currentSelection;
    }

    @Override
    public void update(FieldViewModel checkBoxViewModel) {
    }
}
