package com.dhis2.data.forms.dataentry.fields.spinner;

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

public class SpinnerRow implements Row<SpinnerHolder, SpinnerViewModel> {

    public ViewDataBinding binding;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public SpinnerRow(FlowableProcessor<RowAction> processor){
        this.processor = processor;
    }

    @NonNull
    @Override
    public SpinnerHolder onCreate(@NonNull ViewGroup parent) {
        return new SpinnerHolder(binding);
    }

    @Override
    public void onBind(@NonNull SpinnerHolder viewHolder, @NonNull SpinnerViewModel viewModel) {

    }

}
