package org.dhis2.data.forms.dataentry.tablefields.spinner;

import androidx.fragment.app.FragmentActivity;

import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FormOptionSetBinding;
import org.dhis2.utils.customviews.OptionSetCellPopUp;
import org.dhis2.utils.optionset.OptionSetDialog;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends FormViewHolder {

    FormOptionSetBinding binding;
    private final FlowableProcessor<RowAction> processor;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;

    private SpinnerViewModel viewModel;
    private boolean editable;
    SpinnerHolder(FormOptionSetBinding mBinding, FlowableProcessor<RowAction> processor, FlowableProcessor<Trio<String, String, Integer>> processorOptionSet, boolean isSearchMode) {
        super(mBinding);
        this.binding = mBinding;
        this.processor = processor;
        this.processorOptionSet = processorOptionSet;

        binding.optionSetView.setOnSelectedOptionListener((optionName, optionCode) -> {

            processor.onNext(
                    RowAction.create(viewModel.uid(), optionCode, optionName, viewModel.dataElement(),
                            viewModel.categoryOptionCombo(), viewModel.catCombo(), viewModel.row(), viewModel.column())
            );
        });
    }

    public void update(SpinnerViewModel viewModel, boolean accessDataWrite) {
        this.viewModel = viewModel;
        this.editable = viewModel.editable() && accessDataWrite;

        binding.optionSetView.updateEditable(editable);
        binding.optionSetView.setValue(viewModel.value());
    }

    public void dispose() {
    }

    @Override
    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);
        if (selectionState == SelectionState.SELECTED && editable) {
            closeKeyboard(binding.optionSetView);

            OptionSetDialog dialog = new OptionSetDialog();
            dialog.create(itemView.getContext());
            dialog.setOptionSetTable(viewModel);

            if (dialog.showDialog()) {
                dialog.setListener(binding.optionSetView);
                dialog.setClearListener((view) -> binding.optionSetView.deleteSelectedOption());
                dialog.show(((FragmentActivity) binding.getRoot().getContext()).getSupportFragmentManager(), OptionSetDialog.Companion.getTAG());
            } else {
                dialog.dismiss();
                new OptionSetCellPopUp(itemView.getContext(), binding.optionSetView, viewModel,
                        binding.optionSetView);
            }
        }
    }
}
