package org.dhis2.uicomponents.map.carousel

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.io.File
import org.dhis2.R
import org.dhis2.databinding.ItemCarouselEventBinding
import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.DateUtils
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

class CarouselEventHolder(
    val binding: ItemCarouselEventBinding,
    val program: Program?,
    val onClick: (teiUid: String?, enrollmentUid: String?) -> Boolean
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<EventUiComponentModel> {

    override fun bind(data: EventUiComponentModel) {
        val attribute: String
        data.teiAttribute.filter { it.value != null }.apply {
            attribute =
                "${keys.first()}: ${(get(keys.first()) as TrackedEntityAttributeValue).value()}"
        }
        val eventInfo =
            DateUtils.getInstance().formatDate(data.event.eventDate() ?: data.event.dueDate())

        binding.apply {
            event = data.event
            program = this@CarouselEventHolder.program
            enrollment = data.enrollment
            programStage = data.programStage
            teiAttribute.text = attribute
            this.eventInfo.text = eventInfo
        }

        itemView.setOnClickListener {
            onClick(data.enrollment.trackedEntityInstance(), data.enrollment.uid())
        }

        setStageStyle(
            data.programStage?.style()?.color(),
            data.programStage?.style()?.icon(),
            binding.programStageImage
        )
        setImage(data.teiImage, data.teiDefaultIcon, binding.teiImage)
    }

    private fun setImage(image: String, default: String, target: ImageView) {
        Glide.with(itemView.context).load(File(image))
            .placeholder(
                ResourceManager(target.context)
                    .getObjectStyleDrawableResource(default, R.drawable.photo_temp_gray)
            )
            .error(
                ResourceManager(target.context)
                    .getObjectStyleDrawableResource(default, R.drawable.photo_temp_gray)
            )
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(target)
    }

    private fun setStageStyle(color: String?, icon: String?, target: ImageView) {
        val stageColor = ColorUtils.getColorFrom(
            color,
            ColorUtils.getPrimaryColor(
                target.context,
                ColorUtils.ColorType.PRIMARY_LIGHT
            )
        )
        target.apply {
            background = ColorUtils.tintDrawableWithColor(
                target.background,
                stageColor
            )
            setImageResource(
                ResourceManager(target.context).getObjectStyleDrawableResource(
                    icon,
                    R.drawable.ic_program_default
                )
            )
            setColorFilter(ColorUtils.getContrastColor(stageColor))
        }
    }
}
