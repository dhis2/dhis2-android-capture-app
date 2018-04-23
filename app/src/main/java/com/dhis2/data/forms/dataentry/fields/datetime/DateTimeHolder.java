package com.dhis2.data.forms.dataentry.fields.datetime;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormDateTextBinding;
import com.dhis2.databinding.FormDateTimeTextBinding;
import com.dhis2.databinding.FormTimeTextBinding;
import com.dhis2.utils.DateUtils;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;


/**
 * Created by frodriguez on 16/01/2018.
 */

public class DateTimeHolder extends FormViewHolder {

    private final CompositeDisposable disposable;
    @NonNull
    private BehaviorProcessor<DateTimeViewModel> model;

    public DateTimeHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.disposable = new CompositeDisposable();

        model = BehaviorProcessor.create();

        if (binding instanceof FormTimeTextBinding) {
            ((FormTimeTextBinding) binding).timeView.setDateListener(date -> processor.onNext(
                    RowAction.create(model.getValue().uid(), date != null ? DateUtils.databaseDateFormat().format(date) : null)
            ));
        }

        if (binding instanceof FormDateTextBinding) {
            ((FormDateTextBinding) binding).dateView.setDateListener(date -> processor.onNext(
                    RowAction.create(model.getValue().uid(), date != null ? DateUtils.databaseDateFormat().format(date) : null)
            ));
        }

        if (binding instanceof FormDateTimeTextBinding) {
            ((FormDateTimeTextBinding) binding).dateTimeView.setDateListener(date -> processor.onNext(
                    RowAction.create(model.getValue().uid(), date != null ? DateUtils.databaseDateFormat().format(date) : null)
            ));
        }

        disposable.add(
                model.subscribe(
                        dateTimeViewModel -> {
                            binding.setVariable(BR.label, dateTimeViewModel.label());
                            if (!isEmpty(dateTimeViewModel.value()))
                                binding.setVariable(BR.initData, dateTimeViewModel.value());
                            binding.executePendingBindings();
                        },
                        Timber::d)
        );
    }


    public void update(DateTimeViewModel viewModel) {
        model.onNext(viewModel);
    }

}