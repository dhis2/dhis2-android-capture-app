package org.dhis2.uicomponents.map.carousel

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.io.File
import org.dhis2.R
import org.dhis2.databinding.ItemCarouselRelationshipBinding
import org.dhis2.uicomponents.map.model.RelationshipMapModel
import org.dhis2.uicomponents.map.model.TeiMap
import org.dhis2.utils.ObjectStyleUtils

class CarouselRelationshipHolder(
    val binding: ItemCarouselRelationshipBinding,
    val delete: (String) -> Unit
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<RelationshipMapModel> {

    override fun bind(data: RelationshipMapModel) {
        binding.clearButton.visibility = if (data.canBeDeleted == true) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.clearButton.setOnClickListener {
            delete(data.relationshipUid)
        }
        binding.relationshipTypeName.text = data.displayName
        setImage(data.from, binding.fromTeiImage)
        setImage(data.to, binding.toTeiImage)
    }

    private fun setImage(tei: TeiMap, target: ImageView) {
        val placeholderDrawable = placeholderDrawable(tei.defaultIcon)
        Glide.with(itemView.context).load(File(tei.image))
            .placeholder(placeholderDrawable)
            .error(placeholderDrawable)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(target)
    }

    private fun placeholderDrawable(resourceName: String): Drawable {
        return ObjectStyleUtils.getIconResource(
            itemView.context,
            resourceName,
            R.drawable.photo_temp_gray
        )
    }
}
