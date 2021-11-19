package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.resources.setItemPic
import org.dhis2.databinding.ItemRelationshipBinding

class RelationshipViewHolder(private val binding: ItemRelationshipBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(presenter: RelationshipPresenter, relationships: RelationshipViewModel) {
        binding.apply {
            relationshipCard.setOnClickListener {
                if (relationships.canBeOpened) {
                    presenter.onRelationshipClicked(
                        relationships.ownerType,
                        relationships.ownerUid
                    )
                }
            }
            clearButton.apply {
                visibility = if (relationships.canBeOpened) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                setOnClickListener {
                    relationships.relationship.uid()?.let { presenter.deleteRelationship(it) }
                }
            }
            relationshipTypeName.text = relationships.displayRelationshipTypeName()
            toRelationshipName.text = relationships.displayRelationshipName()
            relationships.displayImage().let { (imagePath, defaultRes) ->
                toTeiImage.setItemPic(
                    imagePath,
                    defaultRes,
                    relationships.ownerDefaultColorResource,
                    relationships.displayRelationshipName(),
                    relationships.isEvent(),
                    binding.imageText
                )
            }
        }
    }
}
