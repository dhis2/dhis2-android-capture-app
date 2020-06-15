package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.ItemRelationshipBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 05/12/2017.
 */

public class RelationshipAdapter extends RecyclerView.Adapter<RelationshipViewHolder> {

    private RelationshipPresenter presenter;
    private List<RelationshipViewModel> relationships;

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

    public void addItems(List<RelationshipViewModel> relationships) {
        this.relationships = relationships;
        notifyDataSetChanged();
    }
}
