package com.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemRelationshipBinding;

import org.hisp.dhis.android.core.relationship.Relationship;

import java.util.List;

/**
 * Created by ppajuelo on 05/12/2017.
 */

public class RelationshipAdapter extends RecyclerView.Adapter<RelationshipViewHolder> {


    private List<Relationship> relationships;

    public RelationshipAdapter() {

    }

    @Override
    public RelationshipViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemRelationshipBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_relationship, parent, false);
        return new RelationshipViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RelationshipViewHolder holder, int position) {
        holder.bind(relationships.get(position));
    }

    @Override
    public int getItemCount() {
        return relationships != null ? relationships.size() : 0;
    }

    public void addItems(List<Relationship> relationships) {
        this.relationships = relationships;
        notifyDataSetChanged();
    }
}
