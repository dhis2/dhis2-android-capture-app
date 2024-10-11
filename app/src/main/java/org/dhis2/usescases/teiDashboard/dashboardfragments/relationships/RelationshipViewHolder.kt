package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.view.View
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemRelationshipBinding

// TODO not remove unitl delete relationship in https://dhis2.atlassian.net/browse/ANDROAPP-6364 is implemented
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
                    /*binding.composeToImage.setUpMetadataIcon(
                        relationships.ownerStyle,
                        false,
                    )*/
                } else {
                    /*toTeiImage.setItemPic(
                        imagePath,
                        defaultRes,
                        relationships.ownerStyle.color.toArgb(),
                        relationships.displayRelationshipName(),
                        relationships.isEvent(),
                        binding.imageText,
                        colorUtils,
                    )*/
                }
            }
        }
    }
}
