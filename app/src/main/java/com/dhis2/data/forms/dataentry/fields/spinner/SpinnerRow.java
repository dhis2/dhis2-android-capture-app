package com.dhis2.data.forms.dataentry.fields.spinner;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class SpinnerRow implements Row<SpinnerHolder, SpinnerViewModel> {

    public ViewDataBinding binding;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public SpinnerRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor) {
        this.processor = processor;
    }

    @NonNull
    @Override
    public SpinnerHolder onCreate(@NonNull ViewGroup parent) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.form_spinner, parent, false);
        return new SpinnerHolder(binding);
    }

    @Override
    public void onBind(@NonNull SpinnerHolder viewHolder, @NonNull SpinnerViewModel viewModel) {

    }

}
