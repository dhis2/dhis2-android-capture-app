package com.dhis2.data.forms.dataentry.fields.radiobutton;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormYesNoBinding;
import com.dhis2.utils.Preconditions;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxRadioGroup;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018.
 */

public class RadioButtonHolder extends RecyclerView.ViewHolder {

    @NonNull
    private
    BehaviorProcessor<RadioButtonViewModel> model;
    final RadioGroup radioGroup;

    RadioButtonHolder(ViewGroup parent, FormYesNoBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding.getRoot());
        radioGroup = binding.customYesNo.getRadioGroup();

        model = BehaviorProcessor.create();

        model.subscribe(checkBoxViewModel -> {
            binding.setLabel(checkBoxViewModel.label());
            if (checkBoxViewModel.value() != null && Boolean.valueOf(checkBoxViewModel.value()))
                binding.customYesNo.getRadioGroup().check(R.id.yes);
            else if (checkBoxViewModel.value() != null)
                binding.customYesNo.getRadioGroup().check(R.id.no);
            else
                binding.customYesNo.getRadioGroup().check(R.id.no_value);
        });

        RxRadioGroup.checkedChanges(radioGroup).takeUntil(RxView.detaches(parent))
                .map(checkId -> binding.customYesNo.getRadioGroup().getCheckedRadioButtonId() == checkId ? RadioButtonViewModel.Value.CHECKED :
                        RadioButtonViewModel.Value.UNCHECKED)
                .filter(value -> model.hasValue())
                .filter(value -> !Preconditions.equals(
                        model.getValue().value(), value))
                .map(value -> RowAction.create(model.getValue().uid(), value.toString()))
                .subscribe(
                        processor::onNext,
                        Timber::d);
    }

    public void update(RadioButtonViewModel viewModel) {

        model.onNext(viewModel);

    }
}
