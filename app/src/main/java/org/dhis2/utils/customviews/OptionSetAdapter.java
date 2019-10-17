package org.dhis2.utils.customviews;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import org.dhis2.R;
import org.dhis2.databinding.ItemOptionBinding;
import org.hisp.dhis.android.core.option.Option;

public class OptionSetAdapter extends PagedListAdapter<Option, OptionSetViewHolder> {

    private OptionSetOnClickListener listener;

    OptionSetAdapter(OptionSetOnClickListener listener) {
        super(new DiffUtil.ItemCallback<Option>() {
            @Override
            public boolean areItemsTheSame(@NonNull Option oldItem, @NonNull Option newItem) {
                return oldItem == newItem;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Option oldItem, @NonNull Option newItem) {
                return oldItem.uid().equals(newItem.uid());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public OptionSetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        ItemOptionBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_option, viewGroup, false);
        return new OptionSetViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull OptionSetViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }
}
