package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemRelationshipBinding;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 05/12/2017.
 */

public class RelationshipViewHolder extends RecyclerView.ViewHolder {

    private final ItemRelationshipBinding binding;

    public RelationshipViewHolder(ItemRelationshipBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(RelationshipContracts.Presenter presenter, RelationshipViewModel relationships) {

        Relationship relationship = relationships.relationship();

        boolean from = relationships.relationshipDirection() == RelationshipViewModel.RelationshipDirection.FROM;

        binding.teiRelationshipLink.setOnClickListener(view -> presenter.openDashboard(relationships.teiUid()));

        binding.setPresenter(presenter);
        binding.setRelationship(relationship);
        String relationshipNameText = from ? relationships.relationshipType().toFromName() : relationships.relationshipType().fromToName();
        binding.relationshipName.setText(relationshipNameText != null ? relationshipNameText : relationships.relationshipType().displayName());

        if (relationships.teiAttributes() != null)
            setAttributes(relationships.teiAttributes());
    }

    private void setAttributes(List<TrackedEntityAttributeValue> trackedEntityAttributeValueModels) {
        if (trackedEntityAttributeValueModels.size() > 1)
            binding.setTeiName(String.format("%s %s", trackedEntityAttributeValueModels.get(0).value(), trackedEntityAttributeValueModels.get(1).value()));
        else if (!trackedEntityAttributeValueModels.isEmpty())
            binding.setTeiName(trackedEntityAttributeValueModels.get(0).value());
        else
            binding.setTeiName("-");
        binding.executePendingBindings();
    }
}
