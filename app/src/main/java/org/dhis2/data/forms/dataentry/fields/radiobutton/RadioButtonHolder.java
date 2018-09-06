package org.dhis2.data.forms.dataentry.fields.radiobutton;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormYesNoBinding;

import io.reactivex.processors.FlowableProcessor;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018.
 */

public class RadioButtonHolder extends RecyclerView.ViewHolder {

    private final FlowableProcessor<RowAction> processor;
    /* @NonNull
     private BehaviorProcessor<RadioButtonViewModel> model;*/
    final RadioGroup radioGroup;
    final FormYesNoBinding binding;

    RadioButtonViewModel viewModel;

    RadioButtonHolder(ViewGroup parent, FormYesNoBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding.getRoot());
        radioGroup = binding.customYesNo.getRadioGroup();
        this.binding = binding;
        this.processor = processor;

      /*  model
                .subscribe(checkBoxViewModel -> {
                            StringBuilder label = new StringBuilder(checkBoxViewModel.label());
                            if (checkBoxViewModel.mandatory())
                                label.append("*");
                            binding.setLabel(label.toString());
                            binding.setValueType(checkBoxViewModel.valueType());
                            if (checkBoxViewModel.value() != null && Boolean.valueOf(checkBoxViewModel.value()))
                                binding.customYesNo.getRadioGroup().check(R.id.yes);
                            else if (checkBoxViewModel.value() != null)
                                binding.customYesNo.getRadioGroup().check(R.id.no);
                            else
                                binding.customYesNo.getRadioGroup().check(R.id.no_value);

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
                        },
                        Timber::d);

        RxRadioGroup.checkedChanges(radioGroup).takeUntil(RxView.detaches(parent))
                .filter(checkIc -> model.hasValue())
                .map(checkId ->
                        {
                            switch (checkId) {
                                case R.id.yes:
                                    return RowAction.create(model.getValue().uid(), String.valueOf(true));
                                case R.id.no:
                                    return RowAction.create(model.getValue().uid(), String.valueOf(false));
                                default:
                                    return RowAction.create(model.getValue().uid(), null);
                            }
                        }
                )
                .subscribe(
                        processor::onNext,
                        Timber::d);*/
    }

    /*private boolean checkValue(RadioButtonViewModel checkBoxViewModel) {
        return model.getValue() == null || !model.getValue().equals(checkBoxViewModel);
    }*/

    public void update(RadioButtonViewModel checkBoxViewModel) {

//        model.onNext(viewModel);

        this.viewModel = checkBoxViewModel;

        radioGroup.setOnCheckedChangeListener(null);

        StringBuilder label = new StringBuilder(checkBoxViewModel.label());
        if (checkBoxViewModel.mandatory())
            label.append("*");
        binding.setLabel(label.toString());
        binding.setValueType(checkBoxViewModel.valueType());
        if (checkBoxViewModel.value() != null && Boolean.valueOf(checkBoxViewModel.value()))
            binding.customYesNo.getRadioGroup().check(R.id.yes);
        else if (checkBoxViewModel.value() != null)
            binding.customYesNo.getRadioGroup().check(R.id.no);
        else
            binding.customYesNo.getRadioGroup().check(R.id.no_value);

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

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RowAction rowAction;
                switch (checkedId) {
                    case R.id.yes:
                        rowAction = RowAction.create(viewModel.uid(), String.valueOf(true));
                        break;
                    case R.id.no:
                        rowAction = RowAction.create(viewModel.uid(), String.valueOf(false));
                        break;
                    default:
                        rowAction = RowAction.create(viewModel.uid(), null);
                        break;
                }
                processor.onNext(rowAction);
            }
        });


    }

    public void dispose() {
    }
}
