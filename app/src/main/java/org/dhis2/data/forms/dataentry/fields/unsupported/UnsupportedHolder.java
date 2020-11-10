package org.dhis2.data.forms.dataentry.fields.unsupported;

import androidx.databinding.ViewDataBinding;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.databinding.FormUnsupportedCustomBinding;
import org.dhis2.utils.customviews.UnsupportedView;


public class UnsupportedHolder extends FormViewHolder {

    private final UnsupportedView unsupportedView;

    public UnsupportedHolder(ViewDataBinding binding) {
        super(binding);
        unsupportedView = ((FormUnsupportedCustomBinding) binding).unsupportedView;
    }

    @Override
    public void update(FieldViewModel viewModel) {
        unsupportedView.setLabel(viewModel.label());
        descriptionText = viewModel.description();
        label = new StringBuilder().append(viewModel.label());

        setFormFieldBackground();
    }
}
