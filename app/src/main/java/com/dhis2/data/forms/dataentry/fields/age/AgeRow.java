package com.dhis2.data.forms.dataentry.fields.age;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.databinding.AgeCustomViewBinding;

/**
 * Created by frodriguez on 20/03/2018.
 */

public class AgeRow implements Row<AgeHolder, AgeViewModel> {

    @NonNull
    @Override
    public AgeHolder onCreate(@NonNull ViewGroup parent) {
        AgeCustomViewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.form_age_custom, parent, false);
        return new AgeHolder(binding);
    }

    @Override
    public void onBind(@NonNull AgeHolder viewHolder, @NonNull AgeViewModel viewModel) {

    }
}
