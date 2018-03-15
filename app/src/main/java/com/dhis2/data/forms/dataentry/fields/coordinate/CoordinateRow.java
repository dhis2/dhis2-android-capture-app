package com.dhis2.data.forms.dataentry.fields.coordinate;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class CoordinateRow implements Row<CoordinateHolder, CoordinateViewModel> {

    ViewDataBinding binding;
    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public CoordinateRow(FlowableProcessor<RowAction> processor) {
        this.processor = processor;

    }

    @NonNull
    @Override
    public CoordinateHolder onCreate(ViewDataBinding binding, @NonNull ViewGroup parent) {
        return new CoordinateHolder(binding);
    }

    @Override
    public void onBind(@NonNull CoordinateHolder viewHolder, @NonNull CoordinateViewModel viewModel) {

    }

}
