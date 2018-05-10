package com.dhis2.usescases.searchTrackEntity.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchTrackedEntityBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;


/**
 * QUADRAM. Created by frodriguez on 11/7/2017.
 */

public class SearchTEAdapter extends RecyclerView.Adapter<SearchTEViewHolder> {

    private final MetadataRepository metadataRepository;
    private SearchTEContractsModule.Presenter presenter;
    private List<TrackedEntityInstanceModel> trackedEntityInstances;

    public SearchTEAdapter(SearchTEContractsModule.Presenter presenter, MetadataRepository metadataRepository) {
        this.presenter = presenter;
        this.metadataRepository = metadataRepository;
        this.trackedEntityInstances = new ArrayList<>();
        setHasStableIds(true);
    }

    @Override
    public SearchTEViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity, parent, false);
        return new SearchTEViewHolder(binding);

    }

    @Override
    public long getItemId(int position) {
        return trackedEntityInstances.get(position).uid().hashCode();
    }

    @Override
    public void onBindViewHolder(SearchTEViewHolder holder, int position) {
        holder.bind(presenter, trackedEntityInstances.get(position), metadataRepository);
    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    public void setItems(List<TrackedEntityInstanceModel> trackedEntityInstances) {
//        this.trackedEntityInstances.clear();
        this.trackedEntityInstances.addAll(trackedEntityInstances);
        notifyDataSetChanged();
    }

    public void clear() {
        this.trackedEntityInstances.clear();
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        trackedEntityInstances.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, trackedEntityInstances.size());
    }
}
