package org.dhis2.usescases.searchTrackEntity.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.databinding.ItemSearchRelationshipTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * QUADRAM. Created by frodriguez on 4/16/2018.
 */

public class SearchRelationshipAdapter extends RecyclerView.Adapter<SearchRelationshipViewHolder> {

    private final MetadataRepository metadataRepository;
    private SearchTEContractsModule.SearchTEPresenter presenter;
    private List<SearchTeiModel> trackedEntityInstances;

    public SearchRelationshipAdapter(SearchTEContractsModule.SearchTEPresenter presenter, MetadataRepository metadataRepository) {
        this.presenter = presenter;
        this.metadataRepository = metadataRepository;
        this.trackedEntityInstances = new ArrayList<>();
    }

    @NonNull
    @Override
    public SearchRelationshipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchRelationshipTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_relationship_tracked_entity, parent, false);
        return new SearchRelationshipViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRelationshipViewHolder holder, int position) {
        holder.bind(presenter, trackedEntityInstances.get(position), metadataRepository);
    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    public void setItems(List<SearchTeiModel> trackedEntityInstances) {
        this.trackedEntityInstances.addAll(trackedEntityInstances);
        notifyDataSetChanged();
    }

    public void clear() {
        this.trackedEntityInstances.clear();
        notifyDataSetChanged();
    }
}
