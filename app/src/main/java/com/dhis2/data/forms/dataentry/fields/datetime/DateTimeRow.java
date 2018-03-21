package com.dhis2.data.forms.dataentry.fields.datetime;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormDateTextBinding;
import com.dhis2.databinding.FormDateTimeTextBinding;
import com.dhis2.databinding.FormTimeTextBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class DateTimeRow implements Row<DateTimeHolder, DateTimeViewModel> {

    private final int TIME = 5;
    private final int DATE = 6;
    private final int DATETIME = 7;
    private final LayoutInflater inflater;

    private int viewType;

    public DateTimeRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, int viewType) {
        //this.processor = processor;
        this.inflater = layoutInflater;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public DateTimeHolder onCreate(@NonNull ViewGroup parent) {
        if (viewType == TIME) {
            FormTimeTextBinding binding = DataBindingUtil.inflate(inflater,
                    R.layout.form_time_text, parent, false);
            return new DateTimeHolder(binding);
        } else if (viewType == DATE) {
            FormDateTextBinding binding = DataBindingUtil.inflate(inflater,
                    R.layout.form_date_text, parent, false);
            return new DateTimeHolder(binding);
        } else {
            FormDateTimeTextBinding binding = DataBindingUtil.inflate(inflater,
                    R.layout.form_date_time_text, parent, false);
            return new DateTimeHolder(binding);
        }
    }

    @Override
    public void onBind(@NonNull DateTimeHolder viewHolder, @NonNull DateTimeViewModel viewModel) {

    }

}
