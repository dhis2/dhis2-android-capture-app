package org.dhis2.usescases.searchTrackEntity.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import org.dhis2.R;
import org.dhis2.databinding.ItemSearchTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

/**
 * Created by frodriguez on 4/12/2019.
 */
public class SearchTeiLiveAdapter extends PagedListAdapter<SearchTeiModel, SearchTEViewHolder> {

    private static final DiffUtil.ItemCallback<SearchTeiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<SearchTeiModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull SearchTeiModel oldItem, @NonNull SearchTeiModel newItem) {
            return oldItem.getTei().uid().equals(newItem.getTei().uid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SearchTeiModel oldItem, @NonNull SearchTeiModel newItem) {
            if (oldItem.isOnline() && oldItem.getTei().state() == null)
                return oldItem.getTei().uid().equals(newItem.getTei().uid()) &&
                        (oldItem.getTei().state() == null && newItem.getTei().state() == null) &&
                        oldItem.getAttributeValues().equals(newItem.getAttributeValues());
            else {
                return oldItem.getTei().uid().equals(newItem.getTei().uid()) &&
                        oldItem.getTei().state().equals(newItem.getTei().state()) &&
                        oldItem.getAttributeValues().equals(newItem.getAttributeValues()) &&
                        oldItem.getEnrollments().equals(newItem.getEnrollments());
            }
        }
    };
    private SearchTEContractsModule.Presenter presenter;

    public SearchTeiLiveAdapter(SearchTEContractsModule.Presenter presenter) {
        super(DIFF_CALLBACK);
        this.presenter = presenter;
    }

    @NonNull
    @Override
    public SearchTEViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity, parent, false);
        return new SearchTEViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchTEViewHolder holder, int position) {
        holder.bind(presenter, getItem(position));
    }
}
