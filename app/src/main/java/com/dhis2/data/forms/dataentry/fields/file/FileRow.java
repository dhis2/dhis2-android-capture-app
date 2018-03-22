package com.dhis2.data.forms.dataentry.fields.file;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.databinding.FormButtonBinding;

/**
 * Created by ppajuelo on 19/03/2018.
 */

public class FileRow implements Row<FileHolder, FileViewModel> {


    @NonNull
    @Override
    public FileHolder onCreate(@NonNull ViewGroup parent) {
        FormButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.form_button, parent, false);
        return new FileHolder(binding);
    }

    @Override
    public void onBind(@NonNull FileHolder viewHolder, @NonNull FileViewModel viewModel) {

    }
}
