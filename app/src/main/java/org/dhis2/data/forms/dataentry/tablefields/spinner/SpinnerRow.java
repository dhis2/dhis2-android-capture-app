package org.dhis2.data.forms.dataentry.tablefields.spinner;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class SpinnerRow implements Row<SpinnerHolder, SpinnerViewModel> {


    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBackgroundTransparent;
    private final String renderType;
    private final LayoutInflater inflater;
    private boolean accessDataWrite;
    public SpinnerRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent) {
        this.processor = processor;
        this.isBackgroundTransparent = isBackgroundTransparent;
        this.renderType = null;
        this.inflater = layoutInflater;
    }

    public SpinnerRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent, String renderType, boolean accessDataWrite) {
        this.processor = processor;
        this.isBackgroundTransparent = isBackgroundTransparent;
        this.renderType = renderType;
        this.inflater = layoutInflater;
        this.accessDataWrite = accessDataWrite;
    }

    @NonNull
    @Override
    public SpinnerHolder onCreate(@NonNull ViewGroup parent) {
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, isBackgroundTransparent ? R.layout.form_spinner : R.layout.form_spinner_accent, parent, false);
        return new SpinnerHolder(binding, processor, isBackgroundTransparent, renderType);
    }

    @Override
    public void onBind(@NonNull SpinnerHolder viewHolder, @NonNull SpinnerViewModel viewModel, String value) {
        viewHolder.update(viewModel, accessDataWrite);
    }

    @Override
    public void deAttach(@NonNull SpinnerHolder viewHolder) {
        viewHolder.dispose();
    }

}
