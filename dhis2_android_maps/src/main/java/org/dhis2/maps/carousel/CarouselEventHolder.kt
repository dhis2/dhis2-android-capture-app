package org.dhis2.maps.carousel

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.bindings.setTeiImage
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.databinding.ItemCarouselEventBinding
import org.dhis2.maps.model.EventUiComponentModel
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.setUpMetadataIcon
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import javax.inject.Inject

class CarouselEventHolder(
    val binding: ItemCarouselEventBinding,
    val program: Program?,
    val onClick: (teiUid: String?, enrollmentUid: String?, eventUid: String?) -> Boolean,
    private val profileImagePreviewCallback: (String) -> Unit,
    val onNavigate: (teiUid: String) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<EventUiComponentModel> {

    init {
        binding.composeProgramStageIcon.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
    }

    @Inject
    lateinit var colorUtils: ColorUtils

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
            data.metadataIconData,
            binding.composeProgramStageIcon,
        )
        SearchTeiModel().apply {
            setProfilePicture(data.teiImage)
            defaultTypeIcon = data.teiDefaultIcon
            attributeValues = data.teiAttribute
            setTeiImage(
                itemView.context,
                binding.teiImage,
                binding.imageText,
                colorUtils,
                profileImagePreviewCallback,
            )
        }

        binding.mapNavigateFab.setOnClickListener {
            onNavigate(data.eventUid)
        }
    }

    private fun setStageStyle(metadataIconData: MetadataIconData, target: ComposeView) {
        target.setUpMetadataIcon(metadataIconData)
    }

    override fun showNavigateButton() {
        binding.mapNavigateFab.show()
    }

    override fun hideNavigateButton() {
        binding.mapNavigateFab.hide()
    }
}
