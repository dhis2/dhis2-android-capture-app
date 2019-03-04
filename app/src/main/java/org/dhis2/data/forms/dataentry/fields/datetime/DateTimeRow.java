package org.dhis2.data.forms.dataentry.fields.datetime;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormDateTextBinding;
import org.dhis2.databinding.FormDateTimeTextBinding;
import org.dhis2.databinding.FormTimeTextBinding;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class DateTimeRow implements Row<DateTimeHolder, DateTimeViewModel> {

    private static final int TIME = 5;
    private static final int DATE = 6;

    private final LayoutInflater inflater;
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBgTransparent;
    private final FlowableProcessor<Integer> currentPosition;

    private int viewType;

    public DateTimeRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, int viewType, boolean isBgTransparent) {
        this.processor = processor;
        this.inflater = layoutInflater;
        this.viewType = viewType;
        this.isBgTransparent = isBgTransparent;
        this.currentPosition = null;
    }

    public DateTimeRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                       @NonNull FlowableProcessor<Integer> currentPosition, int viewType, boolean isBgTransparent) {
        this.processor = processor;
        this.inflater = layoutInflater;
        this.viewType = viewType;
        this.isBgTransparent = isBgTransparent;
        this.currentPosition = currentPosition;
    }

    @NonNull
    @Override
    public DateTimeHolder onCreate(@NonNull ViewGroup parent) {

        ViewDataBinding binding;

        switch (viewType) {
            case TIME:
                binding = DataBindingUtil.inflate(inflater,
                        R.layout.form_time_text, parent, false);
                ((FormTimeTextBinding) binding).timeView.setIsBgTransparent(isBgTransparent);
                break;
            case DATE:
                binding = DataBindingUtil.inflate(inflater,
                        R.layout.form_date_text, parent, false);
                ((FormDateTextBinding) binding).dateView.setIsBgTransparent(isBgTransparent);
                break;
            default:
                binding = DataBindingUtil.inflate(inflater,
                        R.layout.form_date_time_text, parent, false);
                ((FormDateTimeTextBinding) binding).dateTimeView.setIsBgTransparent(isBgTransparent);
                break;
        }

        return new DateTimeHolder(binding, processor, currentPosition);
    }

    @Override
    public void onBind(@NonNull DateTimeHolder viewHolder, @NonNull DateTimeViewModel viewModel) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull DateTimeHolder viewHolder) {
        viewHolder.dispose();
    }
}