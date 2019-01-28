package org.dhis2.data.forms.dataentry.fields.radiobutton;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormYesNoBinding;

import io.reactivex.processors.FlowableProcessor;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018.
 */

public class RadioButtonHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;

    final RadioGroup radioGroup;
    final FormYesNoBinding binding;
    private final View clearButton;

    RadioButtonViewModel viewModel;

    RadioButtonHolder(ViewGroup parent, FormYesNoBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        radioGroup = binding.customYesNo.getRadioGroup();
        clearButton = binding.customYesNo.getClearButton();
        this.binding = binding;
        this.processor = processor;
    }


    public void update(RadioButtonViewModel checkBoxViewModel) {


        this.viewModel = checkBoxViewModel;

        radioGroup.setOnCheckedChangeListener(null);
        descriptionText = viewModel.description();
        binding.setDescription(descriptionText);
        label = new StringBuilder(checkBoxViewModel.label());
        binding.customYesNo.setValueType(checkBoxViewModel.valueType());
        if (checkBoxViewModel.mandatory())
            label.append("*");
        binding.setLabel(label.toString());
        binding.setValueType(checkBoxViewModel.valueType());
        if (checkBoxViewModel.value() != null && Boolean.valueOf(checkBoxViewModel.value()))
            binding.customYesNo.getRadioGroup().check(R.id.yes);
        else if (checkBoxViewModel.value() != null)
            binding.customYesNo.getRadioGroup().check(R.id.no);
        else
            binding.customYesNo.getRadioGroup().clearCheck();

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

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(checkBoxViewModel.editable());
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RowAction rowAction;
            switch (checkedId) {
                case R.id.yes:
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(true));
                    break;
                case R.id.no:
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(false));
                    break;
                default:
                    rowAction = RowAction.create(checkBoxViewModel.uid(), null);
                    break;
            }
            processor.onNext(rowAction);
        });

        clearButton.setOnClickListener(view -> {
            if (checkBoxViewModel.editable().booleanValue()) {
                radioGroup.clearCheck();
                processor.onNext(RowAction.create(checkBoxViewModel.uid(), null));
            }
        });


    }

    public void dispose() {
    }
}
