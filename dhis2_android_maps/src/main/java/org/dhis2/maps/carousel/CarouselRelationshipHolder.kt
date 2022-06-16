package org.dhis2.maps.carousel

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.io.File
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.maps.databinding.ItemCarouselRelationshipBinding
import org.dhis2.maps.model.RelationshipUiComponentModel
import org.dhis2.maps.model.TeiMap

class CarouselRelationshipHolder(
    val binding: ItemCarouselRelationshipBinding,
    private val currentTei: String,
    val delete: (String) -> Boolean,
    val clickListener: (String, RelationshipOwnerType) -> Boolean,
    val onNavigate: (teiUid: String) -> Unit
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<RelationshipUiComponentModel> {

    override fun bind(data: RelationshipUiComponentModel) {
        binding.clearButton.visibility = if (data.canBeDeleted == true) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.clearButton.setOnClickListener {
            delete(data.relationshipUid)
        }
        binding.relationshipCard.setOnClickListener {
            clickListener(
                if (currentTei == data.from.teiUid) {
                    data.to.teiUid!!
                } else {
                    data.from.teiUid!!
                },
                data.relationshipOwner
            )
        }
        binding.relationshipTypeName.text = data.displayName
        if (currentTei == data.from.teiUid) {
            setImage(data.from, binding.fromTeiImage)
            setImage(data.to, binding.toTeiImage)
        } else {
            setImage(data.to, binding.fromTeiImage)
            setImage(data.from, binding.toTeiImage)
        }

        when (currentTei) {
            "" -> {
                binding.toRelationshipName.text = data.to.mainAttribute
                binding.fromRelationshipName.text = data.from.mainAttribute
            }
            data.from.teiUid -> {
                binding.toRelationshipName.text = data.to.mainAttribute
                binding.fromRelationshipName.visibility = View.GONE
            }
            data.to.teiUid -> {
                binding.toRelationshipName.text = data.from.mainAttribute
                binding.fromRelationshipName.visibility = View.GONE
            }
        }

        binding.mapNavigateFab.setOnClickListener {
            onNavigate(data.to.teiUid ?: "")
        }
    }

    private fun setImage(tei: TeiMap, target: ImageView) {
        Glide.with(itemView.context).load(File(tei.image))
            .placeholder(tei.defaultImage)
            .error(tei.defaultImage)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(target)
    }

    override fun showNavigateButton() {
        binding.mapNavigateFab.show()
    }

    override fun hideNavigateButton() {
        binding.mapNavigateFab.hide()
    }
}
