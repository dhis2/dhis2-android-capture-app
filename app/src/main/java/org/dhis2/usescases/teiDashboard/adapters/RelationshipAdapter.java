package org.dhis2.usescases.teiDashboard.adapters;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemRelationshipBinding;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;

import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipPresenter;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipType;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 05/12/2017.
 */

public class RelationshipAdapter extends RecyclerView.Adapter<RelationshipViewHolder> {

    private RelationshipPresenter presenter;
    private List<Pair<Relationship, RelationshipType>> relationships;

    public RelationshipAdapter(RelationshipPresenter presenter) {
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
