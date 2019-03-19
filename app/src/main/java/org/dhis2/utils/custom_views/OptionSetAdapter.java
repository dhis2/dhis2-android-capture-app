package org.dhis2.utils.custom_views;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemOptionBinding;
import org.hisp.dhis.android.core.option.OptionModel;

import java.util.ArrayList;
import java.util.List;

public class OptionSetAdapter extends RecyclerView.Adapter<OptionSetViewHolder> {

    private List<OptionModel> options;
    private OptionSetOnClickListener listener;

    public OptionSetAdapter(OptionSetOnClickListener listener) {
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

    public void setOptions(List<OptionModel> options, int currentPage) {
        if (currentPage == 0) {
            this.options = options;
            notifyDataSetChanged();
        } else {
            this.options.addAll(options);
            notifyItemRangeInserted(this.options.size()-options.size(),options.size());
        }
    }
}
