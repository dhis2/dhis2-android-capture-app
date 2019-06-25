package org.dhis2.data.forms.dataentry.fields.datetime;

import android.graphics.Color;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormDateTextBinding;
import org.dhis2.databinding.FormDateTimeTextBinding;
import org.dhis2.databinding.FormTimeTextBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.Date;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ViewDataBinding;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;


/**
 * QUADRAM. Created by frodriguez on 16/01/2018.
 */

public class DateTimeHolder extends FormViewHolder implements OnDateSelected {

    private final FlowableProcessor<RowAction> processor;
    private final boolean isSearchMode;

    private DateTimeViewModel dateTimeViewModel;

    DateTimeHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        super(binding);
        this.processor = processor;
        this.isSearchMode = isSearchMode;

        if (binding instanceof FormTimeTextBinding) {
            ((FormTimeTextBinding) binding).timeView.setDateListener(this);
        }

        if (binding instanceof FormDateTextBinding) {
            ((FormDateTextBinding) binding).dateView.setDateListener(this);
        }

        if (binding instanceof FormDateTimeTextBinding) {
            ((FormDateTimeTextBinding) binding).dateTimeView.setDateListener(this);
        }

    }


    public void update(DateTimeViewModel viewModel) {
        this.dateTimeViewModel = viewModel;
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
            if (!isSearchMode)
                itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public void performAction() {
        itemView.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.item_selected_bg));

        if (binding instanceof FormTimeTextBinding)
            ((FormTimeTextBinding) binding).timeView.performOnFocusAction();
        if (binding instanceof FormDateTextBinding)
            ((FormDateTextBinding) binding).dateView.performOnFocusAction();
        if (binding instanceof FormDateTimeTextBinding)
            ((FormDateTimeTextBinding) binding).dateTimeView.performOnFocusAction();
    }
}