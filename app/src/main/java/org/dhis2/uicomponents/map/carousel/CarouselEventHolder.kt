package org.dhis2.uicomponents.map.carousel

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.dhis2.R
import org.dhis2.databinding.ItemCarouselEventBinding
import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.dhis2.uicomponents.map.model.StageStyle
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.DateUtils
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import java.io.File

class CarouselEventHolder(
    val binding: ItemCarouselEventBinding,
    val program: Program?,
    val onClick: (teiUid: String?, enrollmentUid: String?) -> Boolean
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<EventUiComponentModel> {

    override fun bind(data: EventUiComponentModel) {
        binding.event = data.event
        binding.program = program
        binding.enrollment = data.enrollment
        binding.programStage = data.programStage

        itemView.setOnClickListener {
            onClick(data.enrollment.trackedEntityInstance(), data.enrollment.uid())
        }
        val attribute: String
        data.teiAttribute.filter { it.value != null }.apply {
            attribute =
                "${keys.first()}: ${(get(keys.first()) as TrackedEntityAttributeValue).value()}"
        }
        val eventInfo = DateUtils.getInstance().formatDate(data.event.eventDate() ?: data.event.dueDate())
        binding.teiAttribute.text = attribute
        binding.eventInfo.text = eventInfo
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
        target.background = ColorUtils.tintDrawableWithColor(
            target.background,
            stageColor
        )
        target.setImageResource(
            ResourceManager(target.context).getObjectStyleDrawableResource(
                icon,
                R.drawable.ic_program_default
            )
        )
        target.setColorFilter(ColorUtils.getContrastColor(stageColor))
    }

}