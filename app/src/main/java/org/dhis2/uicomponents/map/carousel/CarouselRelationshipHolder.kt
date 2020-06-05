package org.dhis2.uicomponents.map.carousel

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.dhis2.databinding.ItemCarouselRelationshipBinding
import org.dhis2.uicomponents.map.model.RelationshipUiComponentModel
import org.dhis2.uicomponents.map.model.TeiMap
import java.io.File

class CarouselRelationshipHolder(
    val binding: ItemCarouselRelationshipBinding,
    private val currentTei: String,
    val delete: (String) -> Boolean,
    val clickListener: (String) -> Boolean
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
        itemView.setOnClickListener {
            clickListener(
                if (currentTei == data.from.teiUid) {
                    data.to.teiUid!!
                } else {
                    data.from.teiUid!!
                }
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

        binding.toRelationshipName.text = if (currentTei == data.from.teiUid) {
            data.to.mainAttribute
        } else {
            data.from.mainAttribute
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
}
