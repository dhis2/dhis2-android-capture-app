package com.dhis2.data.forms.dataentry.fields.coordinate;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeHolder;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class CoordinateRow implements Row<CoordinateHolder, CoordinateViewModel> {

    ViewDataBinding binding;

    @NonNull
    @Override
    public CoordinateHolder onCreate(@NonNull ViewGroup parent) {
        return new CoordinateHolder(binding);
    }

    @Override
    public void onBind(@NonNull CoordinateHolder viewHolder, @NonNull CoordinateViewModel viewModel) {

    }

}
