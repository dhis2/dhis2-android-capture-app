package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.view.View
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.setItemPic
import org.dhis2.databinding.ItemRelationshipBinding
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.setUpMetadataIcon

class RelationshipViewHolder(
    private val binding: ItemRelationshipBinding,
    private val colorUtils: ColorUtils,
) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.composeToImage.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
    }

    fun bind(presenter: RelationshipPresenter, relationships: RelationshipViewModel) {
        binding.apply {
            relationshipCard.setOnClickListener {
                if (relationships.canBeOpened) {
                    presenter.onRelationshipClicked(
                        relationships.ownerType,
                        relationships.ownerUid,
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
                if (relationships.isEvent()) {
                    binding.composeToImage.setUpMetadataIcon(
                        MetadataIconData.Resource(
                            programColor = relationships.ownerDefaultColorResource,
                            iconResource = defaultRes,
                            sizeInDp = 40,
                        ),
                        false,
                    )
                } else {
                    toTeiImage.setItemPic(
                        imagePath,
                        defaultRes,
                        relationships.ownerDefaultColorResource,
                        relationships.displayRelationshipName(),
                        relationships.isEvent(),
                        binding.imageText,
                        colorUtils,
                    )
                }
            }
        }
    }
}
