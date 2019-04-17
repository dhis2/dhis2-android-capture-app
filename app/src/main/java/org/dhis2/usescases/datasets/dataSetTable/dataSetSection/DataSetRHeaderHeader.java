package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.databinding.ItemDatasetHeaderBinding;
import org.dhis2.databinding.ItemDatasetRowBinding;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetRHeaderHeader extends AbstractViewHolder {

    protected ItemDatasetHeaderBinding binding;

    DataSetRHeaderHeader(ItemDatasetHeaderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String rowHeaderTitle) {
        binding.title.setText(rowHeaderTitle);
    }
}
