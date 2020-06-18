package org.dhis2.uicomponents.map.carousel

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.Bindings.addEnrollmentIcons
import org.dhis2.Bindings.hasFollowUp
import org.dhis2.Bindings.setStatusText
import org.dhis2.Bindings.setTeiImage
import org.dhis2.Bindings.toDateSpan
import org.dhis2.R
import org.dhis2.databinding.ItemCarouselTeiBinding
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel

class CarouselTeiHolder(
    val binding: ItemCarouselTeiBinding,
    val onClick: (teiUid: String, enrollmentUid: String?, isOnline: Boolean) -> Boolean,
    val onSyncClick: (String) -> Boolean
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<SearchTeiModel> {

    override fun bind(data: SearchTeiModel) {
        binding.apply {
            overdue = data.isHasOverdue
            isOnline = data.isOnline
            teiSyncState = data.tei.state()
            attribute = data.attributeValues.values.toList()
            attributeNames = data.attributeValues.keys
            lastUpdated.text = data.tei.lastUpdated().toDateSpan(itemView.context)
            executePendingBindings()
        }

        data.apply {
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
            setTeiImage(itemView.context, binding.trackedEntityImage, binding.imageText)
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

        itemView.setOnClickListener {
            onClick(
                data.tei.uid(),
                if (data.selectedEnrollment != null) data.selectedEnrollment.uid() else null,
                data.isOnline
            )
        }
    }
}
