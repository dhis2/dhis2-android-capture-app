package org.dhis2.data.forms.dataentry.fields.unsupported;

import androidx.core.content.ContextCompat;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.databinding.FormUnsupportedCustomBinding;
import org.dhis2.utils.custom_views.UnsupportedView;


public class UnsupportedHolder extends FormViewHolder {

    private final UnsupportedView unsupportedView;

    public UnsupportedHolder(FormUnsupportedCustomBinding binding) {
        super(binding);
        unsupportedView = binding.unsupportedView;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void performAction() {
        itemView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.item_selected_bg));
        unsupportedView.performOnFocusAction();
    }


    public void update(UnsupportedViewModel viewModel) {
        unsupportedView.setLabel(viewModel.label());
        descriptionText = viewModel.description();
        label = new StringBuilder().append(viewModel.label());
    }
}
