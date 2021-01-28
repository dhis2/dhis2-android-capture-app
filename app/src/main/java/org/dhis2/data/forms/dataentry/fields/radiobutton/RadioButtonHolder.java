package org.dhis2.data.forms.dataentry.fields.radiobutton;

import android.view.View;

import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormYesNoBinding;
import org.dhis2.utils.customviews.YesNoView;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;

import io.reactivex.processors.FlowableProcessor;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018.
 */

public class RadioButtonHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;

    private final FormYesNoBinding binding;
    private final boolean isSearchMode;

    private RadioButtonViewModel viewModel;

    RadioButtonHolder(FormYesNoBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode, MutableLiveData<String> currentSelection) {
        super(binding);
        currentUid = currentSelection;
        this.binding = binding;
        this.processor = processor;
        this.isSearchMode = isSearchMode;

        binding.customYesNo.setActivationListener(() -> setSelectedBackground(isSearchMode));
    }


    public void update(RadioButtonViewModel checkBoxViewModel) {

        this.viewModel = checkBoxViewModel;
        fieldUid = checkBoxViewModel.uid();

        binding.customYesNo.setValueListener(null);

        descriptionText = viewModel.description();
        binding.setDescription(descriptionText);
        label = new StringBuilder(checkBoxViewModel.label());
        binding.customYesNo.setValueType(checkBoxViewModel.valueType());
        binding.customYesNo.setRendering(checkBoxViewModel.renderingType() != null ?
                checkBoxViewModel.renderingType() : ValueTypeRenderingType.DEFAULT);
        if (checkBoxViewModel.mandatory())
            label.append("*");
        binding.setLabel(label.toString());
        binding.setValueType(checkBoxViewModel.valueType());

        binding.customYesNo.setInitialValue(checkBoxViewModel.value());


        if (checkBoxViewModel.warning() != null) {
            binding.warningError.setVisibility(View.VISIBLE);
            binding.warningError.setText(checkBoxViewModel.warning());
        } else if (checkBoxViewModel.error() != null) {
            binding.warningError.setVisibility(View.VISIBLE);
            binding.warningError.setText(checkBoxViewModel.error());
        } else {
            binding.warningError.setVisibility(View.GONE);
            binding.warningError.setText(null);
        }

        binding.customYesNo.setEditable(checkBoxViewModel.editable());

        binding.customYesNo.setValueListener(new YesNoView.OnValueChanged() {
            @Override
            public void onValueChanged(boolean isActive) {
                RowAction rowAction;
                setSelectedBackground(isSearchMode);
                if (isActive) {
                    viewModel = (RadioButtonViewModel) checkBoxViewModel.withValue(String.valueOf(true));
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(true), getAdapterPosition());
                } else {
                    viewModel = (RadioButtonViewModel) checkBoxViewModel.withValue(String.valueOf(false));
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(false), getAdapterPosition());
                }
                binding.customYesNo.nextFocus(binding.customYesNo);
                processor.onNext(rowAction);
                clearBackground(isSearchMode);
            }

            @Override
            public void onClearValue() {
                setSelectedBackground(isSearchMode);
                viewModel = (RadioButtonViewModel) checkBoxViewModel.withValue(null);
                RowAction rowAction = RowAction.create(checkBoxViewModel.uid(), null, getAdapterPosition());
                binding.customYesNo.nextFocus(binding.customYesNo);
                processor.onNext(rowAction);
                clearBackground(isSearchMode);
            }
        });


        initFieldFocus();
    }
}
