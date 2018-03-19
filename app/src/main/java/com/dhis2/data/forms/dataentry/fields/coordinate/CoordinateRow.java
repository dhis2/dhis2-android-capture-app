package com.dhis2.data.forms.dataentry.fields.coordinate;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormCoordinatesBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class CoordinateRow implements Row<CoordinateHolder, CoordinateViewModel> {

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public CoordinateRow(FlowableProcessor<RowAction> processor) {
        this.processor = processor;

    }

    @NonNull
    @Override
    public CoordinateHolder onCreate(@NonNull ViewGroup parent) {
        FormCoordinatesBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.form_coordinates, parent, false);
        return new CoordinateHolder(binding);
    }

    @Override
    public void onBind(@NonNull CoordinateHolder viewHolder, @NonNull CoordinateViewModel viewModel) {

    }

}
