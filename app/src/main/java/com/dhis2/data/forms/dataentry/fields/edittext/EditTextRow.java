package com.dhis2.data.forms.dataentry.fields.edittext;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeHolder;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class EditTextRow implements Row<EditTextCustomHolder, EditTextViewModel> {

    ViewDataBinding binding;

    @NonNull
    @Override
    public EditTextCustomHolder onCreate(@NonNull ViewGroup parent) {
        return new EditTextCustomHolder(binding);
    }

    @Override
    public void onBind(@NonNull EditTextCustomHolder viewHolder, @NonNull EditTextViewModel viewModel) {

    }

}
