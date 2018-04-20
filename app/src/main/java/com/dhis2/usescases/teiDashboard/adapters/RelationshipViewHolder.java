package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.BR;
import com.dhis2.databinding.ItemRelationshipBinding;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;

import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipModel;

/**
 * Created by ppajuelo on 05/12/2017.
 */

public class RelationshipViewHolder extends RecyclerView.ViewHolder {


    private final ItemRelationshipBinding binding;

    public RelationshipViewHolder(ItemRelationshipBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TeiDashboardContracts.Presenter presenter, RelationshipModel relationship) {
        binding.setPresenter(presenter);
        binding.setRelationship(relationship);
        binding.executePendingBindings();
    }
}
