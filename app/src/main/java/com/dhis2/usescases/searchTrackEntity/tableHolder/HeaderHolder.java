package com.dhis2.usescases.searchTrackEntity.tableHolder;

import com.dhis2.databinding.ItemTableHeaderBinding;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

/**
 * Created by ppajuelo on 07/03/2018.
 */

public class HeaderHolder extends AbstractViewHolder {
    ItemTableHeaderBinding binding;

    public HeaderHolder(ItemTableHeaderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String p_jValue) {
        binding.setHeaderTitle(p_jValue);
        binding.executePendingBindings();
    }
}
