package com.dhis2.data.forms.dataentry.fields.radiobutton;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormYesNoBinding;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxRadioGroup;

import io.reactivex.disposables.CompositeDisposable;
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
    private CompositeDisposable disposable;

    RadioButtonHolder(ViewGroup parent, FormYesNoBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding.getRoot());
        disposable = new CompositeDisposable();
        radioGroup = binding.customYesNo.getRadioGroup();

        model = BehaviorProcessor.create();

        disposable.add(model.subscribe(checkBoxViewModel -> {
                    StringBuilder label = new StringBuilder(checkBoxViewModel.label());
                    if(checkBoxViewModel.mandatory())
                        label.append("*");
                    binding.setLabel(label.toString());
                    binding.setValueType(checkBoxViewModel.valueType());
                    if (checkBoxViewModel.value() != null && Boolean.valueOf(checkBoxViewModel.value()))
                        binding.customYesNo.getRadioGroup().check(R.id.yes);
                    else if (checkBoxViewModel.value() != null)
                        binding.customYesNo.getRadioGroup().check(R.id.no);
                    else
                        binding.customYesNo.getRadioGroup().check(R.id.no_value);
                },
                Timber::d)
        );

        disposable.add(RxRadioGroup.checkedChanges(radioGroup).takeUntil(RxView.detaches(parent))
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
                        Timber::d));
    }

    public void update(RadioButtonViewModel viewModel) {

        model.onNext(viewModel);

    }
}
