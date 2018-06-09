package com.dhis2.usescases.searchTrackEntity.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchTrackedEntityOnlineBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchTEOnlineAdapter extends RecyclerView.Adapter<SearchTEViewHolderOnline> {

    private final MetadataRepository metadataRepository;
    private SearchTEContractsModule.Presenter presenter;
    private List<TrackedEntityInstanceModel> trackedEntityInstances;
    private HashMap<String, List<String>> teiAttributes = new HashMap<>();

    public SearchTEOnlineAdapter(SearchTEContractsModule.Presenter presenter, MetadataRepository metadataRepository) {
        this.presenter = presenter;
        this.metadataRepository = metadataRepository;
        this.trackedEntityInstances = new ArrayList<>();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public SearchTEViewHolderOnline onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        ItemSearchTrackedEntityOnlineBinding bindingOnline = DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity_online, parent, false);
        return new SearchTEViewHolderOnline(bindingOnline);

    }

    @Override
    public long getItemId(int position) {
        return trackedEntityInstances.get(position).uid().hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull SearchTEViewHolderOnline holder, int position) {
        holder.bind(presenter, trackedEntityInstances.get(position), metadataRepository, teiAttributes.get(trackedEntityInstances.get(position).uid()));
    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    public void setItems(List<TrackedEntityInstanceModel> trackedEntityInstances, HashMap<String, List<String>> teiAttributes) {
        if (getItemCount() > 0)
            for (TrackedEntityInstanceModel tei : trackedEntityInstances) {
                if (!this.trackedEntityInstances.contains(tei))
                    this.trackedEntityInstances.add(tei);
            }
        else
            this.trackedEntityInstances.addAll(trackedEntityInstances);
        this.teiAttributes.putAll(teiAttributes);
        notifyDataSetChanged();
    }

    public void clear() {
        this.trackedEntityInstances.clear();
        this.teiAttributes.clear();
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        trackedEntityInstances.remove(position);
        notifyDataSetChanged();
    }
}
