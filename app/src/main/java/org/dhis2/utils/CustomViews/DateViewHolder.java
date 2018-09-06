package org.dhis2.utils.CustomViews;

import android.support.v7.widget.RecyclerView;

import org.dhis2.databinding.ItemDateBinding;

/**
 * Created by ppajuelo on 05/12/2017.
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
