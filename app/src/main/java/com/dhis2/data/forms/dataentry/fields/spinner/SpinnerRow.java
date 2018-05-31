package com.dhis2.data.forms.dataentry.fields.spinner;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormSpinnerBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class SpinnerRow implements Row<SpinnerHolder, SpinnerViewModel> {


    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBackgroundTransparent;
    private final String renderType;

    public SpinnerRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent) {
        this.processor = processor;
        this.isBackgroundTransparent = isBackgroundTransparent;
        this.renderType = null;
    }

    public SpinnerRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent, String renderType) {
        this.processor = processor;
        this.isBackgroundTransparent = isBackgroundTransparent;
        this.renderType = renderType;
    }

    @NonNull
    @Override
    public SpinnerHolder onCreate(@NonNull ViewGroup parent) {
        FormSpinnerBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.form_spinner, parent, false);
        return new SpinnerHolder(binding, processor, isBackgroundTransparent, renderType);
    }

    @Override
    public void onBind(@NonNull SpinnerHolder viewHolder, @NonNull SpinnerViewModel viewModel) {
        viewHolder.update(viewModel);
    }

}
