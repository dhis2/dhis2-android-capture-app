package com.dhis2.data.forms.dataentry.fields.radiobutton;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.YesNoViewBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class RadioButtonRow implements Row<RadioButtonHolder, RadioButtonViewModel> {

    ViewDataBinding binding;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public RadioButtonRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor) {
        this.processor = processor;

    }

    @NonNull
    @Override
    public RadioButtonHolder onCreate(@NonNull ViewGroup parent) {
        YesNoViewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.form_yes_no, parent, false);
        return new RadioButtonHolder(binding);
    }

    @Override
    public void onBind(@NonNull RadioButtonHolder viewHolder, @NonNull RadioButtonViewModel viewModel) {

    }


}
