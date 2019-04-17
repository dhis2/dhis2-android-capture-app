package org.dhis2.data.forms.dataentry.tablefields.datetime;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FormDateTextBinding;
import org.dhis2.databinding.FormDateTimeTextBinding;
import org.dhis2.databinding.FormTimeTextBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class DateTimeRow implements Row<DateTimeHolder, DateTimeViewModel> {

    private final int TIME = 5;
    private final int DATE = 6;
    private final LayoutInflater inflater;
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBgTransparent;
    private final ObservableField<DataSetTableAdapter.TableScale> tableScale;
    private boolean accessDataWrite;
    private int viewType;


    public DateTimeRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, int viewType, boolean isBgTransparent, boolean accessDataWrite, ObservableField<DataSetTableAdapter.TableScale> currentTableScale) {
        this.processor = processor;
        this.inflater = layoutInflater;
        this.viewType = viewType;
        this.isBgTransparent = isBgTransparent;
        this.accessDataWrite = accessDataWrite;
        this.tableScale = currentTableScale;
    }

    @NonNull
    @Override
    public DateTimeHolder onCreate(@NonNull ViewGroup parent) {

        ViewDataBinding binding;

        switch (viewType) {
            case TIME:
                binding = DataBindingUtil.inflate(inflater,
                        R.layout.form_time_text, parent, false);
                ((FormTimeTextBinding) binding).timeView.setCellLayout(tableScale);
                break;
            case DATE:
                binding = DataBindingUtil.inflate(inflater,
                        R.layout.form_date_text, parent, false);
                ((FormDateTextBinding) binding).dateView.setCellLayout(tableScale);
                break;
            default:
                binding = DataBindingUtil.inflate(inflater,
                        R.layout.form_date_time_text, parent, false);
                ((FormDateTimeTextBinding) binding).dateTimeView.setCellLayout(tableScale);
                break;
        }

        return new DateTimeHolder(binding, processor);
    }

    @Override
    public void onBind(@NonNull DateTimeHolder viewHolder, @NonNull DateTimeViewModel viewModel, String value) {
        viewHolder.update(viewModel, accessDataWrite, value);
    }

    @Override
    public void deAttach(@NonNull DateTimeHolder viewHolder) {
        viewHolder.dispose();
    }
}
