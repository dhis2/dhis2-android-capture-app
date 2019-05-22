package org.dhis2.data.forms.dataentry.fields.spinner;

import android.view.View;

import androidx.fragment.app.FragmentActivity;

import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FormOptionSetBinding;
import org.dhis2.utils.custom_views.OptionSetDialog;
import org.dhis2.utils.custom_views.OptionSetPopUp;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends FormViewHolder implements View.OnClickListener {

    private FormOptionSetBinding binding;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;

    private SpinnerViewModel viewModel;

    SpinnerHolder(FormOptionSetBinding binding, FlowableProcessor<RowAction> processor, FlowableProcessor<Trio<String, String, Integer>> processorOptionSet, boolean isSearchMode) {
        super(binding);
        this.binding = binding;

        this.processorOptionSet = processorOptionSet;

        binding.optionSetView.setOnSelectedOptionListener((optionName, optionCode) -> {
            processor.onNext(
                    RowAction.create(viewModel.uid(), isSearchMode ? optionName + "_os_" + optionCode : optionCode, true, optionCode, optionName)
            );
            if (isSearchMode)
                viewModel.withValue(optionName);
        });

    }


    public void update(SpinnerViewModel viewModel) {
        this.viewModel = viewModel;
        binding.optionSetView.setNumberOfOptions(viewModel.numberOfOptions());
        binding.optionSetView.setObjectStyle(viewModel.objectStyle());
        binding.optionSetView.updateEditable(viewModel.editable());
        binding.optionSetView.setValue(viewModel.value());
        binding.optionSetView.setWarning(viewModel.warning(), viewModel.error());
        binding.optionSetView.setLabel(viewModel.label(), viewModel.mandatory());
        descriptionText = viewModel.description();
        binding.optionSetView.setDescription(descriptionText);
        binding.optionSetView.setOnClickListener(this);
    }

    public void dispose() {
    }

    @Override
    public void onClick(View v) {
        closeKeyboard(v);
        if (binding.optionSetView.openOptionDialog()) {
            OptionSetDialog dialog = OptionSetDialog.newInstance();
            dialog
                    .setProcessor(processorOptionSet)
                    .setOptionSetUid(viewModel)
                    .setOnClick(binding.optionSetView)
                    .setCancelListener(view -> dialog.dismiss())
                    .setClearListener(view -> {
                                binding.optionSetView.deleteSelectedOption();
                                dialog.dismiss();
                            }
                    ).show(((FragmentActivity) binding.getRoot().getContext()).getSupportFragmentManager(), null);
        } else {
            OptionSetPopUp.getInstance()
                    .setOptionSetUid(viewModel)
                    .setProcessor(processorOptionSet)
                    .setOnClick(binding.optionSetView)
                    .show(itemView.getContext(), v);
        }
    }
}
