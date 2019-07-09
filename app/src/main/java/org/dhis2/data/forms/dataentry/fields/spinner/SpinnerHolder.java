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

    private FormOptionSetBinding binding;

    private SpinnerViewModel viewModel;

    SpinnerHolder(FormOptionSetBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        super(binding);
        this.binding = binding;

        binding.optionSetView.setOnSelectedOptionListener((optionName, optionCode) -> {
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
        binding.optionSetView.setNumberOfOptions(viewModel.numberOfOptions());
        binding.optionSetView.setObjectStyle(viewModel.objectStyle());
        binding.optionSetView.updateEditable(viewModel.editable());
        binding.optionSetView.setValue(viewModel.value());
        binding.optionSetView.setWarning(viewModel.warning(), viewModel.error());
        binding.optionSetView.setLabel(viewModel.label(), viewModel.mandatory());
        descriptionText = viewModel.description();
        binding.optionSetView.setDescription(descriptionText);
        binding.optionSetView.setOnClickListener(this);
        label = new StringBuilder().append(viewModel.label());
    }

    public void dispose() {
    }

    @Override
    public void onClick(View v) {
        closeKeyboard(v);
        if (binding.optionSetView.openOptionDialog()) {
            OptionSetDialog dialog = new OptionSetDialog(viewModel,
                    binding.optionSetView,
                    (view) -> binding.optionSetView.deleteSelectedOption()
            );
            if (dialog.isDialogShown()) { dialog.dismiss(); }
            dialog.show(((FragmentActivity) binding.getRoot().getContext()).getSupportFragmentManager(), OptionSetDialog.TAG);
        } else
            new OptionSetPopUp(itemView.getContext(), v, viewModel,
                    binding.optionSetView);
    }
}
