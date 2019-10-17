package org.dhis2.data.forms.dataentry.tablefields.spinner;

import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FormOptionSetBinding;
import org.dhis2.utils.customviews.OptionSetCellDialog;
import org.dhis2.utils.customviews.OptionSetCellPopUp;
import org.dhis2.utils.customviews.OptionSetDialog;


import androidx.fragment.app.FragmentActivity;
import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends FormViewHolder {

    FormOptionSetBinding binding;
    private final FlowableProcessor<RowAction> processor;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;

    private SpinnerViewModel viewModel;

    SpinnerHolder(FormOptionSetBinding mBinding, FlowableProcessor<RowAction> processor, FlowableProcessor<Trio<String, String, Integer>> processorOptionSet, boolean isSearchMode) {
        super(mBinding);
        this.binding = mBinding;
        this.processor = processor;
        this.processorOptionSet = processorOptionSet;

        binding.optionSetView.setOnSelectedOptionListener((optionName, optionCode) -> {

            processor.onNext(
                    RowAction.create(viewModel.uid(), optionCode , viewModel.dataElement(),
                            viewModel.categoryOptionCombo(), viewModel.catCombo(), viewModel.row(), viewModel.column())
            );
        });
    }

    public void update(SpinnerViewModel viewModel, boolean accessDataWrite) {
        this.viewModel = viewModel;
        binding.optionSetView.updateEditable(viewModel.editable() && accessDataWrite);
        binding.optionSetView.setValue(viewModel.value());
    }

    public void dispose() {
    }

    @Override
    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);
        if (selectionState == SelectionState.SELECTED) {
            closeKeyboard(binding.optionSetView);
            if (binding.optionSetView.openOptionDialog()) {
                OptionSetCellDialog dialog = new OptionSetCellDialog(viewModel,
                        binding.optionSetView,
                        (view) -> binding.optionSetView.deleteSelectedOption()
                );
                dialog.show(((FragmentActivity) binding.getRoot().getContext()).getSupportFragmentManager(), OptionSetDialog.TAG);
            } else
                new OptionSetCellPopUp(itemView.getContext(), binding.optionSetView, viewModel,
                        binding.optionSetView);
        }
    }
}
