package org.dhis2.usescases.searchTrackEntity.tableHolder;

import org.dhis2.databinding.ItemTableRowBinding;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

/**
 * QUADRAM. Created by ppajuelo on 07/03/2018.
 */

public class RowHolder extends AbstractViewHolder {

    private ItemTableRowBinding binding;

    public RowHolder(ItemTableRowBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(int pNYPosition) {
        binding.setPosition(pNYPosition);
    }
}
