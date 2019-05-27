package org.dhis2.data.forms.dataentry.fields.display;


import androidx.constraintlayout.widget.ConstraintLayout;

import org.dhis2.BR;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.databinding.ItemIndicatorBinding;


public class DisplayHolder extends FormViewHolder {

    private final ItemIndicatorBinding itemIndicatorBinding;

    public DisplayHolder(ItemIndicatorBinding binding) {
        super(binding);
        this.itemIndicatorBinding = binding;
    }


    public void update(DisplayViewModel viewModel) {
        if (viewModel.label().isEmpty()) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) itemIndicatorBinding.guideline.getLayoutParams();
            params.guidePercent = 0;
            itemIndicatorBinding.guideline.setLayoutParams(params);
        } else
            itemIndicatorBinding.setVariable(BR.label, viewModel.label());

        itemIndicatorBinding.setVariable(BR.value, viewModel.value());
        itemIndicatorBinding.setVariable(BR.colorBg, -1);
        itemIndicatorBinding.executePendingBindings();
    }

    @Override
    public void dispose() {
        // unused
    }

    @Override
    public void performAction() {
        // unused
    }
}
