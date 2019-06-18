package org.dhis2.utils.custom_views;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemLoadingBinding;
import org.dhis2.databinding.ItemOptionBinding;
import org.hisp.dhis.android.core.option.OptionModel;

import java.util.ArrayList;
import java.util.List;

public class OptionSetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_OPTION = R.layout.item_option;
    public static final int TYPE_LOADING = R.layout.item_loading;

    private List<OptionModel> options;
    private OptionSetOnClickListener listener;
    int progess;

    public OptionSetAdapter(OptionSetOnClickListener listener) {
        this.options = new ArrayList<>();
        this.listener = listener;
        progess = 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if(viewType == TYPE_OPTION) {
            ItemOptionBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_option, viewGroup, false);
            return new OptionSetViewHolder(binding, listener);
        } else {
            ItemLoadingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_loading, viewGroup, false);
            return new LoadingViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OptionSetViewHolder)
            ((OptionSetViewHolder) holder).bind(options.get(position));
    }

    @Override
    public int getItemCount() {
        return options.size() + this.progess;
    }

    @Override
    public int getItemViewType(int position) {
        return (position >= options.size()) ? TYPE_LOADING : TYPE_OPTION;
    }

    public void setOptions(List<OptionModel> options, int currentPage) {
        progess = 1;
        if (currentPage == 0) {
            this.options = options;
            notifyDataSetChanged();
        } else {
            this.options.addAll(options);
            notifyItemRangeInserted(this.options.size()-options.size(),options.size());
        }
    }

    public void remove(){
        progess = 0;
        notifyDataSetChanged();
    }

   public boolean isLastItemLoading(int position) {
        return getItemViewType(position) == TYPE_LOADING;
   }
}
