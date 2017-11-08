package com.dhis2.usescases.searchTrackEntity;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;


import com.dhis2.R;
import com.dhis2.databinding.ItemSearchTrackedEntityBinding;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchTEAdapter extends RecyclerView.Adapter<SearchTEViewHolder> {

    private SearchTEPresenter presenter;
    private List<TrackedEntityInstance> trackedEntityInstances;

    public SearchTEAdapter(SearchTEPresenter presenter) {
        this.presenter = presenter;
        this.trackedEntityInstances = new ArrayList<>();
    }

    @Override
    public SearchTEViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity, parent, false);
        return new SearchTEViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SearchTEViewHolder holder, int position) {

        TrackedEntityInstance entityInstance = trackedEntityInstances.get(position);

        ArrayList<String> attributes = new ArrayList<>();
        for (TrackedEntityAttributeValue value : entityInstance.trackedEntityAttributeValues()) {
            attributes.add(value.value());
        }
        holder.bind(presenter, trackedEntityInstances.get(position), attributes);
    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    public void addItems(List<TrackedEntityInstance> trackedEntityInstances) {
        if (trackedEntityInstances.size() > 0) {
            this.trackedEntityInstances.addAll(trackedEntityInstances);
        }
        notifyDataSetChanged();

    }

    public void clear(){
        this.trackedEntityInstances.clear();
        notifyDataSetChanged();
    }
}
