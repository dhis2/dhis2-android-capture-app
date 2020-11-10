package org.dhis2.data.forms.dataentry.fields.display;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.ViewDataBinding;

import org.dhis2.BR;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.databinding.ItemIndicatorBinding;

public class DisplayHolder extends FormViewHolder {

    private final ItemIndicatorBinding binding;
    private DisplayViewModel viewModel;

    public DisplayHolder(ViewDataBinding binding) {
        super(binding);
        this.binding = (ItemIndicatorBinding) binding;
    }

    @Override
    public void update(FieldViewModel viewModel) {

        this.viewModel = (DisplayViewModel) viewModel;

        if (viewModel.label().isEmpty()) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.guideline.getLayoutParams();
            params.guidePercent = 0;
            binding.guideline.setLayoutParams(params);
        } else {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.guideline.getLayoutParams();
            params.guidePercent = 0.6f;
            binding.guideline.setLayoutParams(params);
            binding.setVariable(BR.label, viewModel.label());
        }

        binding.setVariable(BR.value, viewModel.value());
        binding.setVariable(BR.colorBg, -1);
        binding.executePendingBindings();

        setFormFieldBackground();
    }
}
