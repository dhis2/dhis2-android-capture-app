package org.dhis2.data.forms.dataentry.fields.radiobutton;

import android.view.View;
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

    private final RadioGroup radioGroup;
    private final FormYesNoBinding formYesNoBinding;
    private final View clearButton;

    RadioButtonHolder(FormYesNoBinding formYesNoBinding, FlowableProcessor<RowAction> processor) {
        super(formYesNoBinding);
        radioGroup = formYesNoBinding.customYesNo.getRadioGroup();
        clearButton = formYesNoBinding.customYesNo.getClearButton();
        this.formYesNoBinding = formYesNoBinding;
        this.processor = processor;
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
        // unused
    }
}