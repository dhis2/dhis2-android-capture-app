package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemRelationshipBinding;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.util.List;

import kotlin.Pair;

public class RelationshipViewHolder extends RecyclerView.ViewHolder {

    private final ItemRelationshipBinding binding;

    public RelationshipViewHolder(ItemRelationshipBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(RelationshipPresenter presenter, RelationshipViewModel relationships) {

        Relationship relationship = relationships.getRelationship();

        boolean from = relationships.getDirection() == RelationshipDirection.FROM;

        binding.teiRelationshipLink.setOnClickListener(view -> presenter.openDashboard(relationships.getOwnerUid()));

        binding.setPresenter(presenter);
        binding.setRelationship(relationship);
        String relationshipNameText = from ? relationships.getRelationshipType().toFromName() : relationships.getRelationshipType().fromToName();
        binding.relationshipName.setText(relationshipNameText != null ? relationshipNameText : relationships.getRelationshipType().displayName());

        if (from && !relationships.getFromValues().isEmpty()) {
            setAttributes(relationships.getFromValues());
        } else if (!from && !relationships.getToValues().isEmpty()) {
            setAttributes(relationships.getToValues());
        }
    }

    private void setAttributes(List<Pair<String,String>> trackedEntityAttributeValueModels) {
        if (trackedEntityAttributeValueModels.size() > 1)
            binding.setTeiName(String.format("%s %s", trackedEntityAttributeValueModels.get(0).getSecond(), trackedEntityAttributeValueModels.get(1).getSecond()));
        else if (!trackedEntityAttributeValueModels.isEmpty())
            binding.setTeiName(trackedEntityAttributeValueModels.get(0).getSecond());
        else
            binding.setTeiName("-");
        binding.executePendingBindings();
    }
}
