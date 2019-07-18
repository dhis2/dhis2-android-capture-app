package org.dhis2.utils.filters;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterBinding;

class FilterHolder extends RecyclerView.ViewHolder {
    ItemFilterBinding binding;

    public FilterHolder(@NonNull ItemFilterBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind() {
        binding.filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_calendar_positive));
        binding.filterTitle.setText("Filter " + getAdapterPosition());
    }
}
