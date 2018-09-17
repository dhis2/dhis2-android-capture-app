package org.dhis2.data.forms.dataentry.fields.datetime;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;

import org.dhis2.BR;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormDateTextBinding;
import org.dhis2.databinding.FormDateTimeTextBinding;
import org.dhis2.databinding.FormTimeTextBinding;
import org.dhis2.utils.DateUtils;

import java.util.Date;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;


/**
 * Created by frodriguez on 16/01/2018.
 */

public class DateTimeHolder extends FormViewHolder implements OnDateSelected {

    private final CompositeDisposable disposable;
    private final FlowableProcessor<RowAction> processor;
    @NonNull
    private BehaviorProcessor<DateTimeViewModel> model;

    DateTimeHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.disposable = new CompositeDisposable();
        this.processor = processor;
        model = BehaviorProcessor.create();

        if (binding instanceof FormTimeTextBinding) {
            ((FormTimeTextBinding) binding).timeView.setDateListener(this);
        }

        if (binding instanceof FormDateTextBinding) {
            ((FormDateTextBinding) binding).dateView.setDateListener(this);
        }

        if (binding instanceof FormDateTimeTextBinding) {
            ((FormDateTimeTextBinding) binding).dateTimeView.setDateListener(this);
        }

        model.subscribe(
                dateTimeViewModel -> {
                    StringBuilder label = new StringBuilder(dateTimeViewModel.label());
                    if (dateTimeViewModel.mandatory())
                        label.append("*");

                    binding.setVariable(BR.label, label.toString());

                    if (!isEmpty(dateTimeViewModel.value())) {
                        binding.setVariable(BR.initData, dateTimeViewModel.value());
                    } else {
                        binding.setVariable(BR.initData, null);
                    }

                    if (binding instanceof FormDateTextBinding)
                        ((FormDateTextBinding) binding).dateView.setAllowFutureDates(dateTimeViewModel.allowFutureDate());
                    if (binding instanceof FormDateTimeTextBinding)
                        ((FormDateTimeTextBinding) binding).dateTimeView.setAllowFutureDates(dateTimeViewModel.allowFutureDate());

                    if (dateTimeViewModel.warning() != null) {
                        if (binding instanceof FormTimeTextBinding)
                            ((FormTimeTextBinding) binding).timeView.setWarningOrError(dateTimeViewModel.warning());
                        if (binding instanceof FormDateTextBinding)
                            ((FormDateTextBinding) binding).dateView.setWarningOrError(dateTimeViewModel.warning());
                        if (binding instanceof FormDateTimeTextBinding)
                            ((FormDateTimeTextBinding) binding).dateTimeView.setWarningOrError(dateTimeViewModel.warning());

                    } else if (dateTimeViewModel.error() != null) {
                        if (binding instanceof FormTimeTextBinding)
                            ((FormTimeTextBinding) binding).timeView.setWarningOrError(dateTimeViewModel.error());
                        if (binding instanceof FormDateTextBinding)
                            ((FormDateTextBinding) binding).dateView.setWarningOrError(dateTimeViewModel.error());
                        if (binding instanceof FormDateTimeTextBinding)
                            ((FormDateTimeTextBinding) binding).dateTimeView.setWarningOrError(dateTimeViewModel.error());

                    } else {
                        if (binding instanceof FormTimeTextBinding)
                            ((FormTimeTextBinding) binding).timeView.setWarningOrError(null);
                        if (binding instanceof FormDateTextBinding)
                            ((FormDateTextBinding) binding).dateView.setWarningOrError(null);
                        if (binding instanceof FormDateTimeTextBinding)
                            ((FormDateTimeTextBinding) binding).dateTimeView.setWarningOrError(null);
                    }

                    if (binding instanceof FormTimeTextBinding)
                        ((FormTimeTextBinding) binding).timeView.setEditable(dateTimeViewModel.editable());
                    if (binding instanceof FormDateTextBinding)
                        ((FormDateTextBinding) binding).dateView.setEditable(dateTimeViewModel.editable());
                    if (binding instanceof FormDateTimeTextBinding)
                        ((FormDateTimeTextBinding) binding).dateTimeView.setEditable(dateTimeViewModel.editable());

                    binding.executePendingBindings();
                },
                Timber::d
        );
    }


    public void update(DateTimeViewModel viewModel) {
        model.onNext(viewModel);
    }

    @Override
    public void onDateSelected(Date date) {
        processor.onNext(
                RowAction.create(model.getValue().uid(), date != null ? DateUtils.databaseDateFormat().format(date) : null)
        );
    }

    @Override
    public void dispose() {
        disposable.clear();
    }
}