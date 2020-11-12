package org.dhis2.data.forms.dataentry.fields.orgUnit;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class OrgUnitHolder extends FormViewHolder {

    public OrgUnitHolder(ViewDataBinding binding, MutableLiveData<String> currentSelection) {
        super(binding);
        this.currentUid = currentSelection;
    }

    @Override
    public void update(FieldViewModel viewModel) {
    }
}
