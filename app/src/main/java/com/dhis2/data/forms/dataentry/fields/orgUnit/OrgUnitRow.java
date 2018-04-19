package com.dhis2.data.forms.dataentry.fields.orgUnit;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormButtonBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by ppajuelo on 19/03/2018.
 */

public class OrgUnitRow implements Row<OrgUnitHolder, OrgUnitViewModel> {

    private final boolean isBgTransparent;
    private final FlowableProcessor<RowAction> processor;
    private final LayoutInflater inflater;
    private FormButtonBinding binding;

    public OrgUnitRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
    }

    @NonNull
    @Override
    public OrgUnitHolder onCreate(@NonNull ViewGroup parent) {
        binding = DataBindingUtil.inflate(inflater, R.layout.form_button, parent, false);
        if (isBgTransparent)
            binding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorPrimary));
        else
            binding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorAccent));
        return new OrgUnitHolder(binding, processor);
    }

    @Override
    public void onBind(@NonNull OrgUnitHolder viewHolder, @NonNull OrgUnitViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
