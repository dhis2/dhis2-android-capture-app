package org.dhis2.maps.carousel

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.Bindings.setTeiImage
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.maps.R
import org.dhis2.maps.databinding.ItemCarouselEventBinding
import org.dhis2.maps.model.EventUiComponentModel
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.setUpMetadataIcon
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

    init {
        binding.composeProgramStageIcon.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

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
            binding.composeProgramStageIcon
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

        binding.mapNavigateFab.setOnClickListener {
            onNavigate(data.eventUid)
        }
    }

    private fun setStageStyle(color: String?, icon: String?, target: ComposeView) {
        val stageColor = ColorUtils.getColorFrom(
            color,
            ColorUtils.getPrimaryColor(
                target.context,
                ColorUtils.ColorType.PRIMARY_LIGHT
            )
        )
        val resource = ResourceManager(target.context).getObjectStyleDrawableResource(
            icon,
            R.drawable.ic_default_outline
        )
        target.setUpMetadataIcon(
            MetadataIconData(
                stageColor,
                resource,
                40
            )
        )
    }

    override fun showNavigateButton() {
        binding.mapNavigateFab.show()
    }

    override fun hideNavigateButton() {
        binding.mapNavigateFab.hide()
    }
}
