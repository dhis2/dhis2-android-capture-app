package com.dhis2.data.forms.dataentry.fields.datetime;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.view.View;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormDateTextBinding;
import com.dhis2.databinding.FormDateTimeTextBinding;
import com.dhis2.databinding.FormTimeTextBinding;
import com.dhis2.utils.DateUtils;

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
    private TextInputEditText editText;
    @NonNull
    private BehaviorProcessor<DateTimeViewModel> model;

    public DateTimeHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.disposable = new CompositeDisposable();
        this.processor = processor;
        model = BehaviorProcessor.create();

        if (binding instanceof FormTimeTextBinding) {
            ((FormTimeTextBinding) binding).timeView.setDateListener(this);
            this.editText = ((FormTimeTextBinding) binding).timeView.getEditText();
        }

        if (binding instanceof FormDateTextBinding) {
            ((FormDateTextBinding) binding).dateView.setDateListener(this);
            this.editText = ((FormDateTextBinding) binding).dateView.getEditText();
        }

        if (binding instanceof FormDateTimeTextBinding) {
            ((FormDateTimeTextBinding) binding).dateTimeView.setDateListener(this);
            this.editText = ((FormDateTimeTextBinding) binding).dateTimeView.getEditText();
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

    @Override
    public void onDateSelected(Date date) {
        processor.onNext(
                RowAction.create(model.getValue().uid(), date != null ? DateUtils.databaseDateFormat().format(date) : null)
        );

        editText.focusSearch(View.FOCUS_DOWN).requestFocus();
    }
}