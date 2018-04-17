package com.dhis2.usescases.searchTrackEntity;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchTrackedEntityBinding;
import com.dhis2.databinding.ItemSearchTrackedEntityOnlineBinding;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchTEAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final MetadataRepository metadataRepository;
    private final boolean isOnline;
    private SearchTEContractsModule.Presenter presenter;
    private List<TrackedEntityInstanceModel> trackedEntityInstances;

    SearchTEAdapter(SearchTEContractsModule.Presenter presenter, MetadataRepository metadataRepository, boolean online) {
        this.presenter = presenter;
        this.metadataRepository = metadataRepository;
        this.trackedEntityInstances = new ArrayList<>();
        this.isOnline = online;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (!isOnline) {
            ItemSearchTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity, parent, false);
            return new SearchTEViewHolder(binding);
        } else {
            ItemSearchTrackedEntityOnlineBinding bindingOnline = DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity_online, parent, false);
            return new SearchTEViewHolderOnline(bindingOnline);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SearchTEViewHolder)
            ((SearchTEViewHolder) holder).bind(presenter, trackedEntityInstances.get(position), metadataRepository);
        else
            ((SearchTEViewHolderOnline) holder).bind(presenter, trackedEntityInstances.get(position), metadataRepository);

    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    void setItems(List<TrackedEntityInstanceModel> trackedEntityInstances) {
        this.trackedEntityInstances.clear();
        this.trackedEntityInstances.addAll(trackedEntityInstances);
        notifyDataSetChanged();
    }

    public void clear() {
        this.trackedEntityInstances.clear();
        notifyDataSetChanged();
    }
}
