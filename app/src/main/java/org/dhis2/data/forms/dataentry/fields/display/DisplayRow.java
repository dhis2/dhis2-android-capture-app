package org.dhis2.data.forms.dataentry.fields.display;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.databinding.CustomFormDisplayBinding;
import org.dhis2.databinding.ItemIndicatorBinding;

public class DisplayRow implements Row<DisplayHolder, DisplayViewModel> {

    @NonNull
    private final LayoutInflater inflater;

    public DisplayRow(@NonNull LayoutInflater layoutInflater) {
        this.inflater = layoutInflater;
    }

    @NonNull
    @Override
    public DisplayHolder onCreate(@NonNull ViewGroup parent) {
        CustomFormDisplayBinding binding = DataBindingUtil.inflate(inflater, R.layout.custom_form_display, parent, false);
        binding.formDisplayView.setLayout();
        return new DisplayHolder(binding);
    }

    @Override
    public void onBind(@NonNull DisplayHolder viewHolder, @NonNull DisplayViewModel viewModel) {
        viewHolder.bind(viewModel);
    }
}
