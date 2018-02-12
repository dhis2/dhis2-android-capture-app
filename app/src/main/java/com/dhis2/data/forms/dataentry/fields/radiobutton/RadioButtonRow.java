package com.dhis2.data.forms.dataentry.fields.radiobutton;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeHolder;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class RadioButtonRow implements Row<RadioButtonHolder, RadioButtonViewModel> {

    ViewDataBinding binding;

    public RadioButtonRow(FlowableProcessor<RowAction> processor) {

    }

    @NonNull
    @Override
    public RadioButtonHolder onCreate(@NonNull ViewGroup parent) {
        return new RadioButtonHolder(binding);
    }

    @Override
    public void onBind(@NonNull RadioButtonHolder viewHolder, @NonNull RadioButtonViewModel viewModel) {

    }


}
