package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.databinding.ItemDatasetHeaderBinding;

import androidx.databinding.ObservableField;

public class DataSetRHeaderHeader extends AbstractViewHolder {

    protected ItemDatasetHeaderBinding binding;

    DataSetRHeaderHeader(ItemDatasetHeaderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String rowHeaderTitle, ObservableField<DataSetTableAdapter.TableScale> tableScale) {
        binding.setTableScale(tableScale);
        binding.title.setText(rowHeaderTitle);
    }
}
