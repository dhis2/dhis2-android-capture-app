package org.dhis2.data.forms.dataentry.tablefields.datetime;

import android.databinding.ViewDataBinding;

import org.dhis2.BR;
import org.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FormDateTextBinding;
import org.dhis2.databinding.FormDateTimeTextBinding;
import org.dhis2.databinding.FormTimeTextBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.Date;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;


/**
 * QUADRAM. Created by frodriguez on 16/01/2018.
 */

public class DateTimeHolder extends FormViewHolder implements OnDateSelected {

    private final CompositeDisposable disposable;
    private final FlowableProcessor<RowAction> processor;
    /* @NonNull
     private BehaviorProcessor<DateTimeViewModel> model;*/
    private DateTimeViewModel dateTimeViewModel;

    DateTimeHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.disposable = new CompositeDisposable();
        this.processor = processor;
//        model = BehaviorProcessor.create();

        if (binding instanceof FormTimeTextBinding) {
            ((FormTimeTextBinding) binding).timeView.setDateListener(this);
        }

        if (binding instanceof FormDateTextBinding) {
            ((FormDateTextBinding) binding).dateView.setDateListener(this);
        }

        if (binding instanceof FormDateTimeTextBinding) {
            ((FormDateTimeTextBinding) binding).dateTimeView.setDateListener( this);
        }

    }


    public void update(DateTimeViewModel viewModel) {
        this.dateTimeViewModel = viewModel;
//        model.onNext(viewModel);
        descriptionText = viewModel.description();
        label = new StringBuilder(dateTimeViewModel.label());
        if (dateTimeViewModel.mandatory())
            label.append("*");

        binding.setVariable(BR.label, label.toString());
        binding.setVariable(BR.description, descriptionText);

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
    }

    @Override
    public void onDateSelected(Date date) {
        String dateFormatted = "";
        if (date != null)
            if (dateTimeViewModel.valueType() == ValueType.DATE)
                dateFormatted = DateUtils.uiDateFormat().format(date);
            else if (dateTimeViewModel.valueType() == ValueType.TIME)
                dateFormatted = DateUtils.timeFormat().format(date);
            else {
                dateFormatted = DateUtils.databaseDateFormatNoMillis().format(date);
            }
        processor.onNext(
                RowAction.create(dateTimeViewModel.uid(), date != null ? dateFormatted : null)
        );
    }

    @Override
    public void dispose() {
        disposable.clear();
    }
}