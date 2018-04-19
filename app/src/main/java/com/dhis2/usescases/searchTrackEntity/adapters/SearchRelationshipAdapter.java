package com.dhis2.usescases.searchTrackEntity.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchRelationshipTrackedEntityBinding;
import com.dhis2.databinding.ItemSearchTrackedEntityOnlineBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frodriguez on 4/16/2018.
 */

public class SearchRelationshipAdapter extends RecyclerView.Adapter<SearchRelationshipViewHolder> {

    private final MetadataRepository metadataRepository;
    private SearchTEContractsModule.Presenter presenter;
    private List<TrackedEntityInstanceModel> trackedEntityInstances;
    private final boolean isOnline;

    public SearchRelationshipAdapter(SearchTEContractsModule.Presenter presenter, MetadataRepository metadataRepository, boolean online) {
        this.presenter = presenter;
        this.metadataRepository = metadataRepository;
        this.trackedEntityInstances = new ArrayList<>();
        this.isOnline = online;
    }

    @Override
    public SearchRelationshipViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(!isOnline){
            ItemSearchRelationshipTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_relationship_tracked_entity, parent, false);
            return new SearchRelationshipViewHolder(binding);
        }else {
            ItemSearchRelationshipTrackedEntityBinding bindingOnline = DataBindingUtil.inflate(inflater, R.layout.item_search_relationship_tracked_entity, parent, false);
            return new SearchRelationshipViewHolder(bindingOnline);
        }
    }

    @Override
    public void onBindViewHolder(SearchRelationshipViewHolder holder, int position) {
        if (holder instanceof SearchRelationshipViewHolder)
            ((SearchRelationshipViewHolder) holder).bind(presenter, trackedEntityInstances.get(position), metadataRepository);
        else
            ((SearchRelationshipViewHolder) holder).bind(presenter, trackedEntityInstances.get(position), metadataRepository);

    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    public void setItems(List<TrackedEntityInstanceModel> trackedEntityInstances) {
        this.trackedEntityInstances.clear();
        this.trackedEntityInstances.addAll(trackedEntityInstances);
        notifyDataSetChanged();
    }

    public void clear() {
        this.trackedEntityInstances.clear();
        notifyDataSetChanged();
    }
}
