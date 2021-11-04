package org.dhis2.android_maps.carousel

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import org.dhis2.Bindings.setTeiImage
import org.dhis2.android_maps.R
import org.dhis2.android_maps.databinding.ItemCarouselEventBinding
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.android_maps.model.EventUiComponentModel
import org.dhis2.commons.date.DateUtils
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

class CarouselEventHolder(
    val binding: ItemCarouselEventBinding,
    val program: Program?,
    val onClick: (teiUid: String?, enrollmentUid: String?, eventUid: String?) -> Boolean,
    private val profileImagePreviewCallback: (String) -> Unit,
    val onNavigate: (teiUid: String) -> Unit
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
            "${
            DateUtils.getInstance().formatDate(data.event.eventDate() ?: data.event.dueDate())
            } at ${
            data.orgUnitName
            }"

        binding.apply {
            event = data.event
            program = this@CarouselEventHolder.program
            enrollment = data.enrollment
            programStage = data.programStage
            teiAttribute.text = attribute
            this.eventInfo.text = eventInfo
        }

        binding.eventInfoCard.setOnClickListener {
            onClick(data.enrollment.trackedEntityInstance(), data.enrollment.uid(), data.eventUid)
        }

        setStageStyle(
            data.programStage?.style()?.color(),
            data.programStage?.style()?.icon(),
            binding.programStageImage
        )
        SearchTeiModel().apply {
            setProfilePicture(data.teiImage)
            defaultTypeIcon = data.teiDefaultIcon
            attributeValues = data.teiAttribute
            setTeiImage(
                itemView.context,
                binding.teiImage,
                binding.imageText,
                profileImagePreviewCallback
            )
        }

        if (data.event.geometry() == null) {
            binding.noCoordinatesLabel.root.visibility = View.VISIBLE
            binding.noCoordinatesLabel.noCoordinatesMessage.text =
                itemView.context.getString(R.string.no_coordinates_item).format(
                    itemView.context.getString(R.string.event_event)
                        .toLowerCase(Locale.getDefault())
                )
        } else {
            binding.noCoordinatesLabel.root.visibility = View.GONE
        }

        binding.mapNavigateFab.setOnClickListener {
            onNavigate(data.eventUid)
        }
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

    override fun showNavigateButton() {
        binding.mapNavigateFab.show()
    }

    override fun hideNavigateButton() {
        binding.mapNavigateFab.hide()
    }
}
