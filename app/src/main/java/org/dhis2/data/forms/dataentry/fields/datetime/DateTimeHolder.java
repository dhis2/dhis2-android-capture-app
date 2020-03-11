package org.dhis2.data.forms.dataentry.fields.datetime;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.BR;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormDateTextBinding;
import org.dhis2.databinding.FormDateTimeTextBinding;
import org.dhis2.databinding.FormTimeTextBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.Date;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;


/**
 * QUADRAM. Created by frodriguez on 16/01/2018.
 */

public class DateTimeHolder extends FormViewHolder implements OnDateSelected {

    private final FlowableProcessor<RowAction> processor;
    private final boolean isSearchMode;

    private DateTimeViewModel dateTimeViewModel;

    DateTimeHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode, MutableLiveData<String> currentSelection) {
        super(binding);
        this.processor = processor;
        this.isSearchMode = isSearchMode;
        this.currentUid = currentSelection;

        if (binding instanceof FormTimeTextBinding) {
            ((FormTimeTextBinding) binding).timeView.setDateListener(this);
            ((FormTimeTextBinding) binding).timeView.setActivationListener(() ->
                    setSelectedBackground(isSearchMode));
        }

        if (binding instanceof FormDateTextBinding) {
            ((FormDateTextBinding) binding).dateView.setDateListener(this);
            ((FormDateTextBinding) binding).dateView.setActivationListener(() ->
                    setSelectedBackground(isSearchMode));
        }

        if (binding instanceof FormDateTimeTextBinding) {
            ((FormDateTimeTextBinding) binding).dateTimeView.setDateListener(this);
            ((FormDateTimeTextBinding) binding).dateTimeView.setActivationListener(() ->
                    setSelectedBackground(isSearchMode));
        }
    }


    public void update(DateTimeViewModel viewModel) {
        this.dateTimeViewModel = viewModel;
        fieldUid = viewModel.uid();
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
                ((FormTimeTextBinding) binding).timeView.setWarning(dateTimeViewModel.warning());
            if (binding instanceof FormDateTextBinding)
                ((FormDateTextBinding) binding).dateView.setWarning(dateTimeViewModel.warning());
            if (binding instanceof FormDateTimeTextBinding)
                ((FormDateTimeTextBinding) binding).dateTimeView.setWarning(dateTimeViewModel.warning());

        } else if (dateTimeViewModel.error() != null) {
            if (binding instanceof FormTimeTextBinding)
                ((FormTimeTextBinding) binding).timeView.setError(dateTimeViewModel.error());
            if (binding instanceof FormDateTextBinding)
                ((FormDateTextBinding) binding).dateView.setError(dateTimeViewModel.error());
            if (binding instanceof FormDateTimeTextBinding)
                ((FormDateTimeTextBinding) binding).dateTimeView.setError(dateTimeViewModel.error());

        } else {
            if (binding instanceof FormTimeTextBinding)
                ((FormTimeTextBinding) binding).timeView.setError(null);
            if (binding instanceof FormDateTextBinding)
                ((FormDateTextBinding) binding).dateView.setError(null);
            if (binding instanceof FormDateTimeTextBinding)
                ((FormDateTimeTextBinding) binding).dateTimeView.setError(null);
        }

        if (binding instanceof FormTimeTextBinding)
            ((FormTimeTextBinding) binding).timeView.setEditable(dateTimeViewModel.editable());
        if (binding instanceof FormDateTextBinding)
            ((FormDateTextBinding) binding).dateView.setEditable(dateTimeViewModel.editable());
        if (binding instanceof FormDateTimeTextBinding)
            ((FormDateTimeTextBinding) binding).dateTimeView.setEditable(dateTimeViewModel.editable());

        binding.executePendingBindings();

        initFieldFocus();
    }

    @Override
    public void onDateSelected(Date date) {
        String dateFormatted = "";
        if (date != null) {
            if (dateTimeViewModel.valueType() == ValueType.DATE)
                dateFormatted = DateUtils.uiDateFormat().format(date);
            else if (dateTimeViewModel.valueType() == ValueType.TIME)
                dateFormatted = DateUtils.timeFormat().format(date);
            else {
                dateFormatted = DateUtils.databaseDateFormatNoMillis().format(date);
            }
        }
        RowAction rowAction = RowAction.create(dateTimeViewModel.uid(), date != null ? dateFormatted : null, getAdapterPosition());
        if (processor != null) {
            processor.onNext(rowAction);
            clearBackground(isSearchMode);
        }
    }
}