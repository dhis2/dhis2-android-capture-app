package org.dhis2.data.forms.dataentry.fields.age;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;

/**
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

public class AgeHolder extends FormViewHolder {

    public AgeHolder(ViewDataBinding binding, MutableLiveData<String> currentSelection) {
        super(binding);
        this.currentUid = currentSelection;
    }

    @Override
    public void update(FieldViewModel fieldViewModel) {
    }
}
