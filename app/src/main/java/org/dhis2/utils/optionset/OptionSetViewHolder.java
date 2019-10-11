package org.dhis2.utils.optionset;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemOptionBinding;
import org.dhis2.utils.custom_views.OptionSetOnClickListener;
import org.hisp.dhis.android.core.option.Option;

public class OptionSetViewHolder extends RecyclerView.ViewHolder {

    private ItemOptionBinding binding;

    OptionSetViewHolder(@NonNull ItemOptionBinding itemView) {
        super(itemView.getRoot());
        this.binding = itemView;
    }

    public void bind(Option option, OptionSetOnClickListener listener) {
        binding.setOption(option.displayName());
        binding.executePendingBindings();

        itemView.setOnClickListener(view -> listener.onSelectOption(option));
    }
}
