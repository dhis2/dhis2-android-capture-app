package org.dhis2.data.forms.dataentry.fields.radiobutton;

import android.graphics.Color;
import android.view.View;
import android.widget.RadioGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormYesNoBinding;

import androidx.appcompat.content.res.AppCompatResources;
import io.reactivex.processors.FlowableProcessor;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018.
 */

public class RadioButtonHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;

    final RadioGroup radioGroup;
    final FormYesNoBinding binding;
    private final View clearButton;
    private final boolean isSearchMode;

    RadioButtonViewModel viewModel;

    RadioButtonHolder(FormYesNoBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        super(binding);
        radioGroup = binding.customYesNo.getRadioGroup();
        clearButton = binding.customYesNo.getClearButton();
        this.binding = binding;
        this.processor = processor;
        this.isSearchMode = isSearchMode;
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
                    viewModel = (RadioButtonViewModel) checkBoxViewModel.withValue(String.valueOf(true));
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(true), getAdapterPosition());
                    break;
                case R.id.no:
                    viewModel = (RadioButtonViewModel) checkBoxViewModel.withValue(String.valueOf(false));
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(false), getAdapterPosition());
                    break;
                default:
                    viewModel = (RadioButtonViewModel) checkBoxViewModel.withValue(null);
                    rowAction = RowAction.create(checkBoxViewModel.uid(), null, getAdapterPosition());
                    break;
            }
            binding.customYesNo.nextFocus(binding.customYesNo);
            processor.onNext(rowAction);
            if (!isSearchMode)
                itemView.setBackgroundColor(Color.WHITE);
        });

        clearButton.setOnClickListener(view -> {
            if (checkBoxViewModel.editable().booleanValue()) {
                radioGroup.clearCheck();
                processor.onNext(RowAction.create(checkBoxViewModel.uid(), null, getAdapterPosition()));
            }
        });


    }

    public void dispose() {
    }

}
