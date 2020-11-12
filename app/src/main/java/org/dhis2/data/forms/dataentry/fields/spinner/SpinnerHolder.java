package org.dhis2.data.forms.dataentry.fields.spinner;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends FormViewHolder {

    public SpinnerHolder(ViewDataBinding binding, MutableLiveData<String> currentSelection) {
        super(binding);
        this.currentUid = currentSelection;
    }

    @Override
    public void update(FieldViewModel fieldViewModel) {
    }
}
