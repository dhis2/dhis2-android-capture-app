package org.dhis2.utils.customviews;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemDateBinding;

/**
 * QUADRAM. Created by ppajuelo on 05/12/2017.
 */

class DateViewHolder extends RecyclerView.ViewHolder {

    private final ItemDateBinding binding;

    public DateViewHolder(ItemDateBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String date) {
        binding.setDate(date);
        binding.executePendingBindings();
    }
}
