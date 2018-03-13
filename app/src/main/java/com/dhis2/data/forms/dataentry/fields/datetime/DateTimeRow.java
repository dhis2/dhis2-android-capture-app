package com.dhis2.data.forms.dataentry.fields.datetime;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class DateTimeRow implements Row<DateTimeHolder, DateTimeViewModel> {

    ViewDataBinding binding;


    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public DateTimeRow(@NonNull FlowableProcessor<RowAction> processor) {
        this.processor = processor;
    }

    @NonNull
    @Override
    public DateTimeHolder onCreate(ViewDataBinding binding, @NonNull ViewGroup parent) {
        return new DateTimeHolder(binding);
    }

    @Override
    public void onBind(@NonNull DateTimeHolder viewHolder, @NonNull DateTimeViewModel viewModel) {

    }

}
