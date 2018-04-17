package com.dhis2.usescases.searchTrackEntity.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchRelationshipTrackedEntityBinding;
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

    public SearchRelationshipAdapter(SearchTEContractsModule.Presenter presenter, MetadataRepository metadataRepository) {
        this.presenter = presenter;
        this.metadataRepository = metadataRepository;
        this.trackedEntityInstances = new ArrayList<>();
    }

    @Override
    public SearchRelationshipViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchRelationshipTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_relationship, parent, false);
        return new SearchRelationshipViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SearchRelationshipViewHolder holder, int position) {
        holder.bind(presenter, trackedEntityInstances.get(position), metadataRepository);
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
