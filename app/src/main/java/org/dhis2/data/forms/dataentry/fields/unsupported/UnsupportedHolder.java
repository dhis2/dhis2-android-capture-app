package org.dhis2.data.forms.dataentry.fields.unsupported;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.databinding.FormUnsupportedCustomBinding;
import org.dhis2.utils.customviews.UnsupportedView;


public class UnsupportedHolder extends FormViewHolder {

    private final UnsupportedView unsupportedView;

    public UnsupportedHolder(FormUnsupportedCustomBinding binding) {
        super(binding);
        unsupportedView = binding.unsupportedView;
    }

    @Override
    public void update(FieldViewModel viewModel) {
        unsupportedView.setLabel(viewModel.label());
        descriptionText = viewModel.description();
        label = new StringBuilder().append(viewModel.label());

        setFormFieldBackground();
    }
}
