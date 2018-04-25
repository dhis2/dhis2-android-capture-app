package com.dhis2.data.forms.dataentry.fields.age;

import android.support.annotation.NonNull;
import android.view.View;

import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormAgeCustomBinding;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.OnErrorHandler;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

public class AgeHolder extends FormViewHolder {

    @NonNull
    private BehaviorProcessor<AgeViewModel> model;

    AgeHolder(FormAgeCustomBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        CompositeDisposable disposable = new CompositeDisposable();
        model = BehaviorProcessor.create();

        disposable.add(model.subscribe(ageViewModel -> {
            binding.customAgeview.setLabel(ageViewModel.label());
            if (!isEmpty(ageViewModel.value()))
                binding.customAgeview.setInitialValue(ageViewModel.value());
            binding.executePendingBindings();
        }, OnErrorHandler.create()));

        binding.customAgeview.setAgeChangedListener(ageDate -> {
                    processor.onNext(RowAction.create(model.getValue().uid(), DateUtils.databaseDateFormat().format(ageDate)));
                    if (binding.customAgeview.focusSearch(View.FOCUS_DOWN) != null)
                        binding.customAgeview.focusSearch(View.FOCUS_DOWN).requestFocus();
                }
        );
    }


    public void update(AgeViewModel viewModel) {
        model.onNext(viewModel);
    }
}
