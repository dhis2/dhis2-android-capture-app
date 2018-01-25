package com.dhis2.data.forms.dataentry.fields.spinner;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeHolder;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class SpinnerRow implements Row<SpinnerHolder, SpinnerViewModel> {

    public ViewDataBinding binding;

    @NonNull
    @Override
    public SpinnerHolder onCreate(@NonNull ViewGroup parent) {
        return new SpinnerHolder(binding);
    }

    @Override
    public void onBind(@NonNull SpinnerHolder viewHolder, @NonNull SpinnerViewModel viewModel) {

    }

}
