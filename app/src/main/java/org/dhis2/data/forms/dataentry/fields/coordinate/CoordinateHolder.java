package org.dhis2.data.forms.dataentry.fields.coordinate;


import android.annotation.SuppressLint;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;

public class CoordinateHolder extends FormViewHolder {


    @SuppressLint("CheckResult")
    public CoordinateHolder(ViewDataBinding binding, MutableLiveData<String> currentSelection) {
        super(binding);
        this.currentUid = currentSelection;
    }

    @Override
    public void update(FieldViewModel viewModel) {
    }
}