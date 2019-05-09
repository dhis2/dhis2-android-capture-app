package org.dhis2.data.forms.dataentry.tablefields.age;

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
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

public class AgeRow implements Row<AgeHolder, AgeViewModel> {

    private final LayoutInflater inflater;
    private final FlowableProcessor<RowAction> processor;
    private final ObservableField<DataSetTableAdapter.TableScale> tableScale;
    private boolean accessDataWrite;

    public AgeRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean accessDataWrite, ObservableField<DataSetTableAdapter.TableScale> currentTableScale) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.accessDataWrite = accessDataWrite;
        this.tableScale = currentTableScale;
    }

    @NonNull
    @Override
    public AgeHolder onCreate(@NonNull ViewGroup parent) {
        CustomCellViewBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.custom_cell_view, parent, false);
        binding.setTableScale(tableScale);
        return new AgeHolder(binding, processor, inflater.getContext());
    }

    @Override
    public void onBind(@NonNull AgeHolder viewHolder, @NonNull AgeViewModel viewModel, String value) {
        viewHolder.update(viewModel, accessDataWrite);
    }

    @Override
    public void deAttach(@NonNull AgeHolder viewHolder) {
        viewHolder.dispose();
    }
}
