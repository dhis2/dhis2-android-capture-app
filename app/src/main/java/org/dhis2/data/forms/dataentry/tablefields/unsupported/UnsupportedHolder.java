package org.dhis2.data.forms.dataentry.tablefields.unsupported;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.databinding.FormUnsupportedCellBinding;


public class UnsupportedHolder extends FormViewHolder {

    public UnsupportedHolder(FormUnsupportedCellBinding binding) {
        super(binding);
        textView = binding.formButton;
    }

    @Override
    public void dispose() {

    }


    public void update(UnsupportedViewModel viewModel) {
        super.update(viewModel);
        this.accessDataWrite = false;
        textView.setText(R.string.unsupported_value_type);
        textView.setEnabled(false);
        textView.setActivated(false);
        descriptionText = viewModel.description();
    }

    @Override
    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);
        setBackground();
    }
}
