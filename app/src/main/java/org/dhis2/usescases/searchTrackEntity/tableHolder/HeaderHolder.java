package org.dhis2.usescases.searchTrackEntity.tableHolder;

import org.dhis2.databinding.ItemTableHeaderBinding;
import org.dhis2.utils.ColorUtils;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

/**
 * QUADRAM. Created by ppajuelo on 07/03/2018.
 */

public class HeaderHolder extends AbstractViewHolder {
    ItemTableHeaderBinding binding;

    public HeaderHolder(ItemTableHeaderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String p_jValue) {
        binding.getRoot().setBackgroundColor(ColorUtils.getPrimaryColor(binding.getRoot().getContext(), ColorUtils.ColorType.PRIMARY_LIGHT));
        binding.headerTitle.setTextColor(ColorUtils.getPrimaryColor(binding.getRoot().getContext(), ColorUtils.ColorType.PRIMARY_DARK));
        binding.setHeaderTitle(p_jValue);
        binding.executePendingBindings();
    }
}
