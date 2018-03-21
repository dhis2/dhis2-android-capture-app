package com.dhis2.data.forms.dataentry.fields.coordinate;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.CustomFormCoordinateBinding;
import com.dhis2.databinding.FormCoordinatesBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class CoordinateRow implements Row<CoordinateHolder, CoordinateViewModel> {

    @NonNull
    private final FlowableProcessor<RowAction> processor;
    @NonNull
    private final LayoutInflater inflater;

    public CoordinateRow(@NonNull LayoutInflater layoutInflater,
                         @NonNull FlowableProcessor<RowAction> processor) {
        this.inflater = layoutInflater;
        this.processor = processor;
    }

    @NonNull
    @Override
    public CoordinateHolder onCreate(@NonNull ViewGroup parent) {
        CustomFormCoordinateBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.custom_form_coordinate, parent, false);
        return new CoordinateHolder(binding, processor);
    }

    @Override
    public void onBind(@NonNull CoordinateHolder viewHolder, @NonNull CoordinateViewModel viewModel) {
        viewHolder.update(viewModel);
    }

}
