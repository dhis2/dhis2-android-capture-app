package com.dhis2.data.forms.dataentry.fields.age;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormAgeCustomBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 20/03/2018.
 */

public class AgeRow implements Row<AgeHolder, AgeViewModel> {

    private final LayoutInflater inflater;

    public AgeRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
    }

    @NonNull
    @Override
    public AgeHolder onCreate(@NonNull ViewGroup parent) {
        FormAgeCustomBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.form_age_custom, parent, false);
        return new AgeHolder(binding);
    }

    @Override
    public void onBind(@NonNull AgeHolder viewHolder, @NonNull AgeViewModel viewModel) {

    }
}
