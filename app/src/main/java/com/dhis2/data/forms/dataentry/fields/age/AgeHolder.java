package com.dhis2.data.forms.dataentry.fields.age;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormAgeCustomBinding;
import com.dhis2.utils.DateUtils;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

public class AgeHolder extends FormViewHolder {

    @NonNull
    private BehaviorProcessor<AgeViewModel> model;
    CompositeDisposable disposable;

    AgeHolder(FormAgeCustomBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        disposable = new CompositeDisposable();
        model = BehaviorProcessor.create();

        disposable.add(model.subscribe(ageViewModel -> {
                    StringBuilder label = new StringBuilder(ageViewModel.label());
                    if (ageViewModel.mandatory())
                        label.append("*");
                    binding.customAgeview.setLabel(label.toString());
                    if (!isEmpty(ageViewModel.value())) {
                        binding.customAgeview.setInitialValue(ageViewModel.value());
                    }

                    if (ageViewModel.warning() != null)
                        binding.customAgeview.setWarningOrError(ageViewModel.warning());
                    else if (ageViewModel.error() != null)
                        binding.customAgeview.setWarningOrError(ageViewModel.error());
                    else
                        binding.customAgeview.setWarningOrError(null);

                    binding.customAgeview.setEditable(ageViewModel.editable());

                    binding.executePendingBindings();
                },
                Timber::d));

        binding.customAgeview.setAgeChangedListener(ageDate -> {
                    if (model.getValue().value() == null || !model.getValue().value().equals(DateUtils.databaseDateFormat().format(ageDate)))
                        processor.onNext(RowAction.create(model.getValue().uid(), DateUtils.databaseDateFormat().format(ageDate)));
                }
        );
    }


    public void update(AgeViewModel viewModel) {
        model.onNext(viewModel);
    }

    @Override
    public void dispose() {
        disposable.clear();
    }
}
