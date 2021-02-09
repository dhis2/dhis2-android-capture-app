package org.dhis2.data.forms.dataentry.fields.display;


import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.databinding.ItemIndicatorBinding;


public class DisplayHolder extends FormViewHolder {

    private final ItemIndicatorBinding binding;
    private DisplayViewModel viewModel;

    public DisplayHolder(ItemIndicatorBinding binding) {
        super(binding);
        this.binding = binding;
    }


    public void update(DisplayViewModel viewModel) {


    }
}
