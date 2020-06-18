package org.dhis2.usescases.searchTrackEntity.adapters

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.Bindings.addEnrollmentIcons
import org.dhis2.Bindings.hasFollowUp
import org.dhis2.Bindings.setAttributeList
import org.dhis2.Bindings.setStatusText
import org.dhis2.Bindings.setTeiImage
import org.dhis2.Bindings.toDateSpan
import org.dhis2.R
import org.dhis2.databinding.ItemSearchTrackedEntityBinding
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule

class SearchTEViewHolder(private val binding: ItemSearchTrackedEntityBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        presenter: SearchTEContractsModule.Presenter,
        searchTeiModel: SearchTeiModel
    ) {
        binding.apply {
            overdue = searchTeiModel.isHasOverdue
            isOnline = searchTeiModel.isOnline
            teiSyncState = searchTeiModel.tei.state()
            attribute = searchTeiModel.attributeValues.values.toList()
            attributeNames = searchTeiModel.attributeValues.keys
            lastUpdated.text = searchTeiModel.tei.lastUpdated().toDateSpan(itemView.context)
        }

        searchTeiModel.apply {
            binding.setFollowUp(enrollments.hasFollowUp())
            programInfo.addEnrollmentIcons(
                itemView.context,
                binding.programList,
                if (selectedEnrollment != null) selectedEnrollment.program() else null
            )
            selectedEnrollment.setStatusText(
                itemView.context,
                binding.enrollmentStatus,
                isHasOverdue,
                overdueDate
            )
            setTeiImage(
                itemView.context,
                binding.trackedEntityImage,
                binding.imageText
            )
            attributeValues.setAttributeList(
                binding.attributeList,
                binding.showAttributesButton,
                adapterPosition
            ) { showAttributes ->
                if (showAttributes) {
                    showAttributeList()
                } else {
                    hideAttributeList()
                }
            }
        }

        binding.syncState.setOnClickListener {
            if (searchTeiModel.tei.deleted()!! ||
                searchTeiModel.selectedEnrollment != null && searchTeiModel.selectedEnrollment.deleted()!!
            ) Toast.makeText(
                itemView.context,
                itemView.context.getString(R.string.record_marked_for_deletion),
                Toast.LENGTH_SHORT
            ).show() else presenter.onSyncIconClick(searchTeiModel.tei.uid())
        }
        binding.executePendingBindings()
        itemView.setOnClickListener { view: View? ->
            presenter.onTEIClick(
                searchTeiModel.tei.uid(),
                if (searchTeiModel.selectedEnrollment != null) searchTeiModel.selectedEnrollment.uid() else null,
                searchTeiModel.isOnline
            )
        }
    }

    private fun showAttributeList() {
        binding.attributeBName.visibility = View.GONE
        binding.attributeCName.visibility = View.GONE
        binding.sortingFieldName.visibility = View.GONE
        binding.entityAttribute2.visibility = View.GONE
        binding.entityAttribute3.visibility = View.GONE
        binding.sortingFieldValue.visibility = View.GONE
        binding.attributeList.visibility = View.VISIBLE
    }

    private fun hideAttributeList() {
        binding.attributeList.visibility = View.GONE
        binding.attributeBName.visibility = View.VISIBLE
        binding.attributeCName.visibility = View.VISIBLE
        binding.sortingFieldName.visibility = View.VISIBLE
        binding.entityAttribute2.visibility = View.VISIBLE
        binding.entityAttribute3.visibility = View.VISIBLE
        binding.sortingFieldValue.visibility = View.VISIBLE
    }
}