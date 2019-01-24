package org.dhis2.usescases.searchTrackEntity.adapters;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.databinding.ItemSearchTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

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
    private List<SearchTeiModel> teis;

    public SearchTEAdapter(SearchTEContractsModule.Presenter presenter, MetadataRepository metadataRepository) {
        this.presenter = presenter;
        this.metadataRepository = metadataRepository;
        this.trackedEntityInstances = new ArrayList<>();
        this.teis = new ArrayList<>();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public SearchTEViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity, parent, false);
        return new SearchTEViewHolder(binding);

    }

    @Override
    public long getItemId(int position) {
//        return trackedEntityInstances.get(position).uid().hashCode();
        return teis.get(position).getTei().uid().hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull SearchTEViewHolder holder, int position) {
//        holder.bind(presenter, trackedEntityInstances.get(position), metadataRepository);
        holder.bind(presenter, teis.get(position));
    }

    @Override
    public int getItemCount() {
//        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
        return teis != null ? teis.size() : 0;
    }

    public void setItems(List<TrackedEntityInstanceModel> trackedEntityInstances) {
        this.trackedEntityInstances.clear();
        this.trackedEntityInstances.addAll(trackedEntityInstances);
        notifyDataSetChanged();
    }

    public void setTeis(List<SearchTeiModel> trackedEntityInstances) {
        this.teis.addAll(trackedEntityInstances);
        notifyDataSetChanged();
    }

    public void clear() {
        this.trackedEntityInstances.clear();
        this.teis.clear();
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        trackedEntityInstances.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, trackedEntityInstances.size());
    }
}
