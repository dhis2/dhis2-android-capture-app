package org.dhis2.data.forms.dataentry.tablefields.radiobutton;

import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FormYesNoBinding;

import io.reactivex.processors.FlowableProcessor;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018.
 */

public class RadioButtonCellHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;

    final RadioGroup radioGroup;
    final FormYesNoBinding binding;
    private final View clearButton;

    RadioButtonViewModel viewModel;

    RadioButtonCellHolder(FormYesNoBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        radioGroup = binding.customYesNo.getRadioGroup();
        clearButton = binding.customYesNo.getClearButton();
        this.binding = binding;
        this.processor = processor;
    }


    public void update(RadioButtonViewModel checkBoxViewModel, boolean accessDataWrite) {


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
        else if (checkBoxViewModel.value() != null && !checkBoxViewModel.value().isEmpty())
            binding.customYesNo.getRadioGroup().check(R.id.no);

        if(!(accessDataWrite && checkBoxViewModel.editable())) {
            binding.customYesNo.setBackgroundColor(ContextCompat.getColor(binding.customYesNo.getContext(), R.color.bg_black_e6e));
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(false);
            }
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RowAction rowAction;
            switch (checkedId) {
                case R.id.yes:
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(true), checkBoxViewModel.dataElement(), checkBoxViewModel.listCategoryOption(),checkBoxViewModel.catCombo(), checkBoxViewModel.row(), checkBoxViewModel.column());
                    break;
                case R.id.no:
                    rowAction = RowAction.create(checkBoxViewModel.uid(), String.valueOf(false), checkBoxViewModel.dataElement(), checkBoxViewModel.listCategoryOption(),checkBoxViewModel.catCombo(), checkBoxViewModel.row(), checkBoxViewModel.column());
                    break;
                default:
                    rowAction = RowAction.create(checkBoxViewModel.uid(), null, checkBoxViewModel.dataElement(), checkBoxViewModel.listCategoryOption(),checkBoxViewModel.catCombo(), checkBoxViewModel.row(), checkBoxViewModel.column());
                    break;
            }
            processor.onNext(rowAction);
        });

        clearButton.setOnClickListener(view -> {
            if (checkBoxViewModel.editable().booleanValue()) {
                radioGroup.clearCheck();
                //processor.onNext();
            }
        });


    }

    public void dispose() {
    }
}
