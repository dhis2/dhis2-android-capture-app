package org.dhis2.data.forms.dataentry.fields.spinner;

import android.graphics.Color;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;

import org.dhis2.R;
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

    private FormOptionSetBinding formOptionSetBinding;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;

    private SpinnerViewModel viewModel;

    SpinnerHolder(FormOptionSetBinding binding, FlowableProcessor<RowAction> processor, FlowableProcessor<Trio<String, String, Integer>> processorOptionSet, boolean isSearchMode) {
        super(binding);
        this.formOptionSetBinding = binding;

        this.processorOptionSet = processorOptionSet;

        formOptionSetBinding.optionSetView.setOnSelectedOptionListener((optionName, optionCode) -> {
            processor.onNext(
                    RowAction.create(viewModel.uid(), isSearchMode ? optionName + "_os_" + optionCode : optionCode, true, optionCode, optionName, getAdapterPosition())
            );
            if (isSearchMode)
                viewModel.withValue(optionName);
            if (!isSearchMode)
                itemView.setBackgroundColor(Color.WHITE);
        });

    }


    public void update(SpinnerViewModel viewModel) {
        this.viewModel = viewModel;
        formOptionSetBinding.optionSetView.setNumberOfOptions(viewModel.numberOfOptions());
        formOptionSetBinding.optionSetView.setObjectStyle(viewModel.objectStyle());
        formOptionSetBinding.optionSetView.updateEditable(viewModel.editable());
        formOptionSetBinding.optionSetView.setValue(viewModel.value());
        formOptionSetBinding.optionSetView.setWarning(viewModel.warning(), viewModel.error());
        formOptionSetBinding.optionSetView.setLabel(viewModel.label(), viewModel.mandatory());
        descriptionText = viewModel.description();
        formOptionSetBinding.optionSetView.setDescription(descriptionText);
        formOptionSetBinding.optionSetView.setOnClickListener(this);
        label = new StringBuilder().append(viewModel.label());
    }

    public void dispose() {
        // unused
    }

    @Override
    public void performAction() {
        itemView.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.item_selected_bg));
        formOptionSetBinding.optionSetView.performOnFocusAction();
    }

    @Override
    public void onClick(View v) {
        closeKeyboard(v);
        if (formOptionSetBinding.optionSetView.openOptionDialog()) {
            OptionSetDialog dialog = OptionSetDialog.newInstance();
            dialog
                    .setProcessor(processorOptionSet)
                    .setOptionSetUid(viewModel)
                    .setOnClick(formOptionSetBinding.optionSetView)
                    .setCancelListener(view -> dialog.dismiss())
                    .setClearListener(view -> {
                                formOptionSetBinding.optionSetView.deleteSelectedOption();
                                dialog.dismiss();
                            }
                    ).show(((FragmentActivity) formOptionSetBinding.getRoot().getContext()).getSupportFragmentManager(), null);
        } else {
            OptionSetPopUp.getInstance()
                    .setOptionSetUid(viewModel)
                    .setProcessor(processorOptionSet)
                    .setOnClick(formOptionSetBinding.optionSetView)
                    .show(itemView.getContext(), v);
        }
    }
}
