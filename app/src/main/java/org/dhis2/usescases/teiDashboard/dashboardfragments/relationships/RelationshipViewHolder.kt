package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.view.View
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemRelationshipBinding
import org.dhis2.tracker.relationships.model.RelationshipModel

// TODO not remove until delete relationship in https://dhis2.atlassian.net/browse/ANDROAPP-6364 is implemented
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

    fun bind(presenter: RelationshipPresenter, relationships: RelationshipModel) {
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
            toRelationshipName.text = relationships.displayRelationshipName()
        }
    }
}
