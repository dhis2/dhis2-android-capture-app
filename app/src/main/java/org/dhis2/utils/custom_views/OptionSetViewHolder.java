package org.dhis2.utils.custom_views;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemOptionBinding;

public class OptionSetViewHolder extends RecyclerView.ViewHolder {

    private ItemOptionBinding binding;
    private OptionSetOnClickListener listener;
    public OptionSetViewHolder(@NonNull ItemOptionBinding itemView, OptionSetOnClickListener listener) {
        super(itemView.getRoot());
        this.binding = itemView;
        this.listener = listener;
    }

    public void bind(String option){
        binding.setOption(option);
        binding.executePendingBindings();

        itemView.setOnClickListener(view -> {
            listener.onSelectOption(option);
        });
    }
}
