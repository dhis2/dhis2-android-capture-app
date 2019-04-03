package org.dhis2.data.forms.dataentry.tablefields.unsupported;

import android.widget.Button;

import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.databinding.FormUnsupportedCellBinding;


public class UnsupportedHolder extends FormViewHolder {

    private final Button button;

    public UnsupportedHolder(FormUnsupportedCellBinding binding) {
        super(binding);
        button = binding.formButton;
    }

    @Override
    public void dispose() {

    }


    public void update(UnsupportedViewModel viewModel) {
        button.setText(viewModel.label());
        descriptionText = viewModel.description();
    }
}
