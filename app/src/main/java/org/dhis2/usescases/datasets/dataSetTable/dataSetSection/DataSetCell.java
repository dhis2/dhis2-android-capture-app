package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.databinding.ItemDatasetCellBinding;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetCell extends AbstractViewHolder {

    protected ItemDatasetCellBinding binding;

    DataSetCell(ItemDatasetCellBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String rowHeaderTitle) {
        binding.title.setText(rowHeaderTitle);
    }
}
