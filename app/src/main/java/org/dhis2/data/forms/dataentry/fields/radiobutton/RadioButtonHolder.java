package org.dhis2.data.forms.dataentry.fields.radiobutton;

import android.graphics.Color;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.content.res.AppCompatResources;

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

    private final RadioGroup radioGroup;
    private final FormYesNoBinding formYesNoBinding;
    private final View clearButton;
    private final boolean isSearchMode;

    RadioButtonHolder(FormYesNoBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        super(binding);
        radioGroup = binding.customYesNo.getRadioGroup();
        clearButton = binding.customYesNo.getClearButton();
        this.formYesNoBinding = binding;
        this.processor = processor;
        this.isSearchMode = isSearchMode;
    }


    public void update(RadioButtonViewModel checkBoxViewModel) {

        radioGroup.setOnCheckedChangeListener(null);
        descriptionText = checkBoxViewModel.description();
        formYesNoBinding.setDescription(descriptionText);
        label = new StringBuilder(checkBoxViewModel.label());
        formYesNoBinding.customYesNo.setValueType(checkBoxViewModel.valueType());
        if (checkBoxViewModel.mandatory())
            label.append("*");
        formYesNoBinding.setLabel(label.toString());
        formYesNoBinding.setValueType(checkBoxViewModel.valueType());
        if (checkBoxViewModel.value() != null && Boolean.valueOf(checkBoxViewModel.value()))
            formYesNoBinding.customYesNo.getRadioGroup().check(R.id.yes);
        else if (checkBoxViewModel.value() != null)
            formYesNoBinding.customYesNo.getRadioGroup().check(R.id.no);
        else
            formYesNoBinding.customYesNo.getRadioGroup().clearCheck();

        if (checkBoxViewModel.warning() != null) {
            formYesNoBinding.warningError.setVisibility(View.VISIBLE);
            formYesNoBinding.warningError.setText(checkBoxViewModel.warning());
        } else if (checkBoxViewModel.error() != null) {
            formYesNoBinding.warningError.setVisibility(View.VISIBLE);
            formYesNoBinding.warningError.setText(checkBoxViewModel.error());
        } else {
            formYesNoBinding.warningError.setVisibility(View.GONE);
            formYesNoBinding.warningError.setText(null);
        }

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(checkBoxViewModel.editable());
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RowAction rowAction;
            switch (checkedId) {
                case R.id.yes:
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(true), getAdapterPosition());
                    break;
                case R.id.no:
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(false), getAdapterPosition());
                    break;
                default:
                    rowAction = RowAction.create(checkBoxViewModel.uid(), null, getAdapterPosition());
                    break;
            }
            formYesNoBinding.customYesNo.nextFocus(formYesNoBinding.customYesNo);
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
        // unused
    }

    @Override
    public void performAction() {
        itemView.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.item_selected_bg));
        formYesNoBinding.customYesNo.performOnFocusAction();
    }
}
