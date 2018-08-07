package com.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.ItemRelationshipBinding;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;

import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.relationship.RelationshipType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppajuelo on 05/12/2017.
 */

public class RelationshipAdapter extends RecyclerView.Adapter<RelationshipViewHolder> {

    private TeiDashboardContracts.Presenter presenter;
    private List<Pair<Relationship, RelationshipType>> relationships;

    public RelationshipAdapter(TeiDashboardContracts.Presenter presenter) {
        this.presenter = presenter;
        this.relationships = new ArrayList<>();
    }

    @NonNull
    @Override
    public RelationshipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRelationshipBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_relationship, parent, false);
        return new RelationshipViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RelationshipViewHolder holder, int position) {
        holder.bind(presenter, relationships.get(position));
    }

    @Override
    public int getItemCount() {
        return relationships != null ? relationships.size() : 0;
    }

    public void addItems(List<Pair<Relationship, RelationshipType>> relationships) {
        this.relationships = relationships;
        notifyDataSetChanged();
    }
}
