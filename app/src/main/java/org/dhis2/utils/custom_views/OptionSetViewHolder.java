package org.dhis2.utils.custom_views;

import org.dhis2.databinding.ItemOptionBinding;
import org.hisp.dhis.android.core.option.OptionModel;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OptionSetViewHolder extends RecyclerView.ViewHolder {

    private ItemOptionBinding binding;
    private OptionSetOnClickListener listener;

    public OptionSetViewHolder(@NonNull ItemOptionBinding itemView, OptionSetOnClickListener listener) {
        super(itemView.getRoot());
        this.binding = itemView;
        this.listener = listener;
    }

    public void bind(OptionModel option) {
        binding.setOption(option.displayName());
        binding.executePendingBindings();

        itemView.setOnClickListener(view -> {
            listener.onSelectOption(option);
        });
    }
}
