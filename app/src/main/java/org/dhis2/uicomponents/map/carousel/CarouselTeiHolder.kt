package org.dhis2.uicomponents.map.carousel

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import org.dhis2.Bindings.addEnrollmentIcons
import org.dhis2.Bindings.hasFollowUp
import org.dhis2.Bindings.setAttributeList
import org.dhis2.Bindings.setStatusText
import org.dhis2.Bindings.setTeiImage
import org.dhis2.Bindings.toDateSpan
import org.dhis2.R
import org.dhis2.databinding.ItemCarouselTeiBinding
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel

class CarouselTeiHolder(
    val binding: ItemCarouselTeiBinding,
    val onClick: (teiUid: String, enrollmentUid: String?, isOnline: Boolean) -> Boolean,
    val onSyncClick: (String) -> Boolean,
    val onNavigate: (teiUid: String) -> Unit,
    val profileImagePreviewCallback: (String) -> Unit,
    val attributeVisibilityCallback: (SearchTeiModel) -> Unit
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<SearchTeiModel> {

    private var dataModel: SearchTeiModel? = null

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
            programInfo.addEnrollmentIcons(
                itemView.context,
                binding.programList,
                selectedEnrollment?.program()
            )
            selectedEnrollment?.setStatusText(
                itemView.context,
                binding.enrollmentStatus,
                isHasOverdue,
                overdueDate
            )
            setTeiImage(
                itemView.context,
                binding.trackedEntityImage,
                binding.imageText,
                profileImagePreviewCallback
            )
            attributeValues.setAttributeList(
                binding.attributeList,
                binding.showAttributesButton,
                adapterPosition,
                data.isAttributeListOpen,
                data.sortingKey,
                data.sortingValue,
                data.enrolledOrgUnit
            ) {
                attributeVisibilityCallback(this)
            }
            if (tei.geometry() == null) {
                binding.noCoordinatesLabel.root.visibility = View.VISIBLE
                binding.noCoordinatesLabel.noCoordinatesMessage.text =
                    itemView.context.getString(R.string.no_coordinates_item)
                        .format(teTypeName.toLowerCase(Locale.ROOT))
            } else {
                binding.noCoordinatesLabel.root.visibility = View.GONE
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
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                onSyncClick(data.tei.uid())
            }
        }

        binding.executePendingBindings()

        binding.teiInfoCard.setOnClickListener {
            onClick(
                data.tei.uid(),
                if (data.selectedEnrollment != null) data.selectedEnrollment.uid() else null,
                data.isOnline
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
        dataModel?.setShowNavigationButton(true)
        binding.mapNavigateFab.show()
    }

    override fun hideNavigateButton() {
        dataModel?.setShowNavigationButton(false)
        binding.mapNavigateFab.hide()
    }
}
