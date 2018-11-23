package org.dhis2.utils.CustomViews;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemOptionBinding;

import java.util.ArrayList;
import java.util.List;

public class OptionSetAdapter extends RecyclerView.Adapter<OptionSetViewHolder> {

    private List<String> options;
    private OptionSetOnClickListener listener;
    public OptionSetAdapter(OptionSetOnClickListener listener)
    {
        this.options = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public OptionSetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        ItemOptionBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_option, viewGroup, false);

        return new OptionSetViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionSetViewHolder holder, int position) {
        holder.bind(options.get(position));
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public void setOptions(List<String> options){
        this.options = options;
        this.notifyDataSetChanged();
    }
}
