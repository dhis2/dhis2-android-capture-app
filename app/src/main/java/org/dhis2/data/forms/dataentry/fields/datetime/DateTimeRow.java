package org.dhis2.data.forms.dataentry.fields.datetime;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormDateTextBinding;
import org.dhis2.databinding.FormDateTimeTextBinding;
import org.dhis2.databinding.FormTimeTextBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class DateTimeRow implements Row<DateTimeHolder, DateTimeViewModel> {

    private static final int TIME = 5;
    private static final int DATE = 6;
    private static final int DATETIME = 7;
    private final LayoutInflater inflater;
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBgTransparent;
    private final String renderType;
    private final MutableLiveData<String> currentSelection;

    private int viewType;
    private boolean isSearchMode = false;

    public DateTimeRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, int viewType, boolean isBgTransparent) {
        this.processor = processor;
        this.inflater = layoutInflater;
        this.viewType = viewType;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
        this.isSearchMode = true;
        this.currentSelection = null;
    }

    public DateTimeRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                       int viewType, boolean isBgTransparent, String renderType,
                       MutableLiveData<String> currentSelection) {
        this.processor = processor;
        this.inflater = layoutInflater;
        this.viewType = viewType;
        this.isBgTransparent = isBgTransparent;
        this.renderType = renderType;
        this.currentSelection = currentSelection;
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
            case DATETIME:
            default:
                binding = DataBindingUtil.inflate(inflater,
                        R.layout.form_date_time_text, parent, false);
                ((FormDateTimeTextBinding) binding).dateTimeView.setIsBgTransparent(isBgTransparent);
                break;
        }

        return new DateTimeHolder(binding, processor, isSearchMode, currentSelection);
    }

    @Override
    public void onBind(@NonNull DateTimeHolder viewHolder, @NonNull DateTimeViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
