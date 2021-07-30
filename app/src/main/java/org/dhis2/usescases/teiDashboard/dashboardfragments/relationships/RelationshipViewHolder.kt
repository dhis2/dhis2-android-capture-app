package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.setItemPic
import org.dhis2.databinding.ItemRelationshipBinding

class RelationshipViewHolder(private val binding: ItemRelationshipBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(presenter: RelationshipPresenter, relationships: RelationshipViewModel) {
        binding.apply {
            relationshipCard.setOnClickListener {
                presenter.onRelationshipClicked(
                    relationships.ownerType,
                    relationships.ownerUid
                )
            }
            clearButton.setOnClickListener {
                relationships.relationship.uid()?.let { presenter.deleteRelationship(it) }
            }
            relationshipTypeName.text = relationships.displayRelationshipTypeName()
            toRelationshipName.text = relationships.displayRelationshipName()
            relationships.displayImage().let { (imagePath, defaultRes) ->
                toTeiImage.setItemPic(
                    imagePath,
                    defaultRes,
                    ColorUtils.getPrimaryColor(toTeiImage.context,ColorUtils.ColorType.PRIMARY),
                    relationships.displayRelationshipName(),
                    binding.imageText
                )
            }
        }
    }
}
