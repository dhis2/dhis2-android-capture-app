package org.dhis2.maps.carousel

import android.view.View
import android.widget.Toast
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.bindings.getEnrollmentIconsData
import org.dhis2.bindings.hasFollowUp
import org.dhis2.bindings.paintAllEnrollmentIcons
import org.dhis2.bindings.setAttributeList
import org.dhis2.bindings.setStatusText
import org.dhis2.bindings.setTeiImage
import org.dhis2.commons.data.EnrollmentIconData
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.R
import org.dhis2.maps.databinding.ItemCarouselTeiBinding

class CarouselTeiHolder(
    val binding: ItemCarouselTeiBinding,
    val colorUtils: ColorUtils,
    val onClick: (teiUid: String, enrollmentUid: String?, isOnline: Boolean) -> Boolean,
    val onSyncClick: (String) -> Boolean,
    val onNavigate: (teiUid: String) -> Unit,
    val profileImagePreviewCallback: (String) -> Unit,
    val attributeVisibilityCallback: (SearchTeiModel) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<SearchTeiModel> {

    private var dataModel: SearchTeiModel? = null

    init {
        binding.composeProgramList.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
    }

    override fun bind(data: SearchTeiModel) {
        dataModel = data
        if (data.isAttributeListOpen) {
            showAttributeList()
        } else {
            hideAttributeList()
        }
        binding.apply {
            overdue = data.isHasOverdue
            isOnline = data.isOnline
            orgUnit = data.enrolledOrgUnit
            teiSyncState = data.tei.state()
            attribute = data.attributeValues.values.toList()
            attributeNames = data.attributeValues.keys
            lastUpdated.text = data.tei.lastUpdated().toDateSpan(itemView.context)
            sortingValue = data.sortingValue
            attributeListOpened = data.isAttributeListOpen
            mapNavigateFab.visibility = if (data.shouldShowNavigationButton()) {
                View.VISIBLE
            } else {
                View.GONE
            }
            executePendingBindings()
        }

        data.apply {
            binding.setFollowUp(enrollments.hasFollowUp())
            val enrollmentIconDataList: List<EnrollmentIconData> =
                programInfo.getEnrollmentIconsData(
                    if (selectedEnrollment != null) selectedEnrollment.program() else null,
                    getMetadataIconData(selectedEnrollment.program()),
                )
            enrollmentIconDataList.paintAllEnrollmentIcons(
                binding.composeProgramList,
            )
            selectedEnrollment?.setStatusText(
                itemView.context,
                binding.enrollmentStatus,
                isHasOverdue,
                overdueDate,
            )
            setTeiImage(
                itemView.context,
                binding.trackedEntityImage,
                binding.imageText,
                colorUtils,
                profileImagePreviewCallback,
            )
            attributeValues.setAttributeList(
                binding.attributeList,
                binding.showAttributesButton,
                adapterPosition,
                data.isAttributeListOpen,
                data.sortingKey,
                data.sortingValue,
                data.enrolledOrgUnit,
            ) {
                attributeVisibilityCallback(this)
            }

            binding.sortingFieldName.text = data.sortingKey
            binding.sortingFieldValue.text = data.sortingValue
        }
        binding.syncState.setOnClickListener {
            if (data.tei.deleted() == true ||
                data.selectedEnrollment != null && data.selectedEnrollment.deleted() == true
            ) {
                Toast.makeText(
                    itemView.context,
                    itemView.context.getString(R.string.record_marked_for_deletion),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                onSyncClick(data.selectedEnrollment.uid())
            }
        }

        binding.executePendingBindings()

        binding.teiInfoCard.setOnClickListener {
            onClick(
                data.tei.uid(),
                if (data.selectedEnrollment != null) data.selectedEnrollment.uid() else null,
                data.isOnline,
            )
        }

        binding.mapNavigateFab.setOnClickListener {
            onNavigate(data.tei.uid())
        }
    }

    private fun showAttributeList() {
        binding.attributeBName.visibility = View.GONE
        binding.enrolledOrgUnit.visibility = View.GONE
        binding.sortingFieldName.visibility = View.GONE
        binding.entityAttribute2.visibility = View.GONE
        binding.entityOrgUnit.visibility = View.GONE
        binding.sortingFieldValue.visibility = View.GONE
        binding.attributeList.visibility = View.VISIBLE
    }

    private fun hideAttributeList() {
        binding.attributeList.visibility = View.GONE
        binding.attributeBName.visibility = View.VISIBLE
        binding.enrolledOrgUnit.visibility = View.VISIBLE
        binding.sortingFieldName.visibility = View.VISIBLE
        binding.entityAttribute2.visibility = View.VISIBLE
        binding.entityOrgUnit.visibility = View.VISIBLE
        binding.sortingFieldValue.visibility = View.VISIBLE
    }

    override fun showNavigateButton() {
        if (dataModel?.tei?.geometry() != null) {
            dataModel?.setShowNavigationButton(true)
            binding.mapNavigateFab.show()
        }
    }

    override fun hideNavigateButton() {
        dataModel?.setShowNavigationButton(false)
        binding.mapNavigateFab.hide()
    }
}
