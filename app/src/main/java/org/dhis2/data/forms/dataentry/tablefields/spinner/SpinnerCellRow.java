package org.dhis2.data.forms.dataentry.tablefields.spinner;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.CustomCellViewBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class SpinnerCellRow implements Row<SpinnerHolder, SpinnerViewModel> {


    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final LayoutInflater inflater;
    private boolean accessDataWrite;
    private FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;
    public SpinnerCellRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean accessDataWrite, FlowableProcessor<Trio<String, String, Integer>> processorOptionSet) {
        this.processor = processor;
        this.inflater = layoutInflater;
        this.accessDataWrite = accessDataWrite;
        this.processorOptionSet = processorOptionSet;
    }

    @NonNull
    @Override
    public SpinnerHolder onCreate(@NonNull ViewGroup parent) {
        CustomCellViewBinding binding = DataBindingUtil.inflate(inflater,  R.layout.custom_cell_view , parent, false);
        return new SpinnerHolder(binding, processor, processorOptionSet);
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
