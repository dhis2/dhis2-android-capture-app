package org.dhis2.usescases.searchTrackEntity.adapters

import android.view.View
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.Bindings.getEnrollmentIconsData
import org.dhis2.Bindings.hasFollowUp
import org.dhis2.Bindings.paintAllEnrollmentIcons
import org.dhis2.Bindings.setAttributeList
import org.dhis2.Bindings.setStatusText
import org.dhis2.Bindings.setTeiImage
import org.dhis2.commons.data.EnrollmentIconData
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.toDateSpan
import org.dhis2.databinding.ItemSearchTrackedEntityBinding

abstract class BaseTeiViewHolder(
    private val binding: ItemSearchTrackedEntityBinding
) : RecyclerView.ViewHolder(binding.root) {

    lateinit var teiModel: SearchTeiModel

    abstract fun itemViewClick()

    abstract fun itemConfiguration()

    init {
        binding.composeProgramList.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    fun bind(
        teiModel: SearchTeiModel,
        attributeVisibilityCallback: () -> Unit,
        profileImagePreviewCallback: (String) -> Unit
    ) {
        this.teiModel = teiModel
        if (teiModel.isAttributeListOpen) {
            showAttributeList()
        } else {
            hideAttributeList()
        }

        binding.apply {
            overdue = teiModel.isHasOverdue
            isOnline = teiModel.isOnline
            orgUnit = teiModel.enrolledOrgUnit
            teiSyncState = teiModel.tei.state()
            attribute = teiModel.attributeValues.values.toList()
            attributeNames = teiModel.attributeValues.keys
            attributeListOpened = teiModel.isAttributeListOpen
            lastUpdated.text = teiModel.tei.lastUpdated().toDateSpan(itemView.context)
            sortingValue = teiModel.sortingValue
        }

        teiModel.apply {
            binding.setFollowUp(enrollments.hasFollowUp())
            val enrollmentIconDataList: List<EnrollmentIconData> =
                programInfo.getEnrollmentIconsData(
                    itemView.context,
                    if (selectedEnrollment != null) selectedEnrollment.program() else null
                )
            enrollmentIconDataList.paintAllEnrollmentIcons(
                binding.composeProgramList
            )
            if (selectedEnrollment != null) {
                selectedEnrollment.setStatusText(
                    itemView.context,
                    binding.enrollmentStatus,
                    isHasOverdue,
                    overdueDate
                )
            }
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
                teiModel.isAttributeListOpen,
                teiModel.sortingKey,
                teiModel.sortingValue,
                teiModel.enrolledOrgUnit
            ) {
                attributeVisibilityCallback()
            }
        }

        binding.executePendingBindings()
        itemConfiguration()
        itemViewClick()
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
}
