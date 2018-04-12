package com.dhis2.data.forms.dataentry.fields.radiobutton;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.dhis2.BR;
import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormYesNoBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.utils.Preconditions;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxRadioGroup;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import rx.exceptions.OnErrorNotImplementedException;


/**
 * Created by frodriguez on 18/01/2018.
 */

public class RadioButtonHolder extends RecyclerView.ViewHolder {
    private FormYesNoBinding binding;
    private final FlowableProcessor<RowAction> processor;
    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;

    @NonNull
    BehaviorProcessor<RadioButtonViewModel> model;

    public RadioButtonHolder(ViewGroup parent, FormYesNoBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding.getRoot());
        this.binding = binding;
        this.processor = processor;

        model = BehaviorProcessor.create();

        model.subscribe(checkBoxViewModel -> {
            binding.setLabel(checkBoxViewModel.label());
            if (RadioButtonViewModel.Value.CHECKED.equals(checkBoxViewModel.value()))
                binding.customYesNo.getRadioGroup().check(R.id.yes);
            if (RadioButtonViewModel.Value.CHECKED_NO.equals(checkBoxViewModel.value()))
                binding.customYesNo.getRadioGroup().check(R.id.no);
            else
                binding.customYesNo.getRadioGroup().check(R.id.no_value);
        });

        RxRadioGroup.checkedChanges(binding.customYesNo.getRadioGroup()).takeUntil(RxView.detaches(parent))
                .map(checkId -> binding.customYesNo.getRadioGroup().getCheckedRadioButtonId() == checkId ? RadioButtonViewModel.Value.CHECKED :
                        RadioButtonViewModel.Value.UNCHECKED)
                .filter(value -> model.hasValue())
                .filter(value -> !Preconditions.equals(
                        model.getValue().value(), value))
                .map(value -> RowAction.create(model.getValue().uid(), value.toString()))
                .subscribe(t -> processor.onNext(t), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                });
        binding.customYesNo.getRadioGroup().setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.yes:
                    processor.onNext(
                            RowAction.create(model.getValue().uid(), "true")
                    );
                    break;
                case R.id.no:
                    processor.onNext(
                            RowAction.create(model.getValue().uid(), "false")
                    );
                    break;
                case R.id.no_value:
                    processor.onNext(
                            RowAction.create(model.getValue().uid(), null)
                    );
                    break;
            }
        });
    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();
    }


    public void update(RadioButtonViewModel viewModel) {

        model.onNext(viewModel);

    }
}
