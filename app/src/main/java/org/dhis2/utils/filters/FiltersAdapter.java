package org.dhis2.utils.filters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterBinding;

public class FiltersAdapter extends RecyclerView.Adapter<FilterHolder> {

    public FiltersAdapter() {

    }

    @NonNull
    @Override
    public FilterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_filter, parent, false);
        return new FilterHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
