package org.dhis2.data.forms.dataentry.tablefields.datetime;


import androidx.databinding.ViewDataBinding;

import org.dhis2.BR;
import org.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.TableDateTextBinding;
import org.dhis2.databinding.TableDateTimeTextBinding;
import org.dhis2.databinding.TableTimeTextBinding;
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
    private DateTimeViewModel dateTimeViewModel;
    private Boolean isEditable;

    DateTimeHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.disposable = new CompositeDisposable();
        this.processor = processor;

        if (binding instanceof TableTimeTextBinding) {
            ((TableTimeTextBinding) binding).timeView.setDateListener(this);
        }

        if (binding instanceof TableDateTextBinding) {
            ((TableDateTextBinding) binding).dateView.setDateListener(this);
        }

        if (binding instanceof TableDateTimeTextBinding) {
            ((TableDateTimeTextBinding) binding).dateTimeView.setDateListener(this);
        }

    }


    public void update(DateTimeViewModel viewModel, boolean accessDataWrite, String value) {
        this.dateTimeViewModel = viewModel;
        this.isEditable = accessDataWrite && viewModel.editable();
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

        if (binding instanceof TableDateTextBinding)
            ((TableDateTextBinding) binding).dateView.setAllowFutureDates(dateTimeViewModel.allowFutureDate());
        if (binding instanceof TableDateTimeTextBinding)
            ((TableDateTimeTextBinding) binding).dateTimeView.setAllowFutureDates(dateTimeViewModel.allowFutureDate());

        if (dateTimeViewModel.warning() != null) {
            if (binding instanceof TableTimeTextBinding)
                ((TableTimeTextBinding) binding).timeView.setWarning(dateTimeViewModel.warning());
            if (binding instanceof TableDateTextBinding)
                ((TableDateTextBinding) binding).dateView.setWarning(dateTimeViewModel.warning());
            if (binding instanceof TableDateTimeTextBinding)
                ((TableDateTimeTextBinding) binding).dateTimeView.setWarning(dateTimeViewModel.warning());

        } else if (dateTimeViewModel.error() != null) {
            if (binding instanceof TableTimeTextBinding)
                ((TableTimeTextBinding) binding).timeView.setError(dateTimeViewModel.error());
            if (binding instanceof TableDateTextBinding)
                ((TableDateTextBinding) binding).dateView.setError(dateTimeViewModel.error());
            if (binding instanceof TableDateTimeTextBinding)
                ((TableDateTimeTextBinding) binding).dateTimeView.setWarning(dateTimeViewModel.error());

        } else {
            if (binding instanceof TableTimeTextBinding) {
                ((TableTimeTextBinding) binding).timeView.setError(null);
                ((TableTimeTextBinding) binding).timeView.setWarning(null);
            }
            if (binding instanceof TableDateTextBinding) {
                ((TableDateTextBinding) binding).dateView.setError(null);
                ((TableDateTextBinding) binding).dateView.setWarning(null);
            }
            if (binding instanceof TableDateTimeTextBinding) {
                ((TableDateTimeTextBinding) binding).dateTimeView.setError(null);
                ((TableDateTimeTextBinding) binding).dateTimeView.setWarning(null);
            }
        }

        if (binding instanceof TableTimeTextBinding)
            ((TableTimeTextBinding) binding).timeView.getEditText().setEnabled(isEditable);
        if (binding instanceof TableDateTextBinding)
            ((TableDateTextBinding) binding).dateView.getEditText().setEnabled(isEditable);
        if (binding instanceof TableDateTimeTextBinding)
            ((TableDateTimeTextBinding) binding).dateTimeView.getEditText().setEnabled(isEditable);


        if (dateTimeViewModel.mandatory()) {
            if (binding instanceof TableTimeTextBinding)
                ((TableTimeTextBinding) binding).timeView.setMandatory();
            if (binding instanceof TableDateTextBinding)
                ((TableDateTextBinding) binding).dateView.setMandatory();
            if (binding instanceof TableDateTimeTextBinding)
                ((TableDateTimeTextBinding) binding).dateTimeView.setMandatory();
        }

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
                dateFormatted = DateUtils.databaseDateFormatNoSeconds().format(date);
            }
        processor.onNext(
                RowAction.create(dateTimeViewModel.uid(), date != null ? dateFormatted : null, dateTimeViewModel.dataElement(), dateTimeViewModel.categoryOptionCombo(), dateTimeViewModel.catCombo(), dateTimeViewModel.row(), dateTimeViewModel.column())
        );
    }

    @Override
    public void dispose() {
        disposable.clear();
    }

    @Override
    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);
        if (selectionState == SelectionState.SELECTED && isEditable) {
            if (binding instanceof TableTimeTextBinding) {
                ((TableTimeTextBinding) binding).timeView.getEditText().performClick();
            }

            if (binding instanceof TableDateTextBinding) {
                ((TableDateTextBinding) binding).dateView.getEditText().performClick();
            }

            if (binding instanceof TableDateTimeTextBinding) {
                ((TableDateTimeTextBinding) binding).dateTimeView.getEditText().performClick();
            }
        }
    }

}