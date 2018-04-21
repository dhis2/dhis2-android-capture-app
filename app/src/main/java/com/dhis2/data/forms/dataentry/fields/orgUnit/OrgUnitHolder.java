package com.dhis2.data.forms.dataentry.fields.orgUnit;

import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormButtonBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by ppajuelo on 19/03/2018.
 */

public class OrgUnitHolder extends FormViewHolder {
    FormButtonBinding binding;

    public OrgUnitHolder(FormButtonBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);

        binding.formButton.setOnClickListener(view -> {
        });
    }

    public void update(OrgUnitViewModel viewModel) {
       /* binding.setLabel(viewModel.label());
        if (viewModel.value() != null)
            binding.formButton.setText(viewModel.value());*/
    }
}
