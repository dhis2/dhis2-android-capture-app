package org.dhis2.data.forms.dataentry.tablefields.coordinate;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;

import androidx.databinding.ObservableField;
import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class CoordinateRow implements Row<CoordinateHolder, CoordinateViewModel> {

    @NonNull
    private final FlowableProcessor<RowAction> processor;
    @NonNull
    private final LayoutInflater inflater;
    private final ObservableField<DataSetTableAdapter.TableScale> tableScale;
    private boolean accessDataWrite;

    public CoordinateRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean accessDataWrite, ObservableField<DataSetTableAdapter.TableScale> currentTableScale) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.accessDataWrite = accessDataWrite;
        this.tableScale = currentTableScale;
    }

    @NonNull
    @Override
    public CoordinateHolder onCreate(@NonNull ViewGroup parent) {
        CustomCellViewBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.custom_cell_view, parent, false);
        binding.setTableScale(tableScale);
        return new CoordinateHolder(binding, processor, inflater.getContext());
    }

    @Override
    public void onBind(@NonNull CoordinateHolder viewHolder, @NonNull CoordinateViewModel viewModel, String value) {
        viewHolder.update(viewModel, accessDataWrite);
    }

    @Override
    public void deAttach(@NonNull CoordinateHolder viewHolder) {
        viewHolder.dispose();
    }
}
