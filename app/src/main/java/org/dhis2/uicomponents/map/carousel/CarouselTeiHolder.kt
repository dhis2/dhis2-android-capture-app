package org.dhis2.uicomponents.map.carousel

import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import org.dhis2.R
import org.dhis2.databinding.ItemCarouselTeiBinding
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.dhis2.utils.ObjectStyleUtils
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import java.io.File

class CarouselTeiHolder(
    val binding: ItemCarouselTeiBinding,
    val onClick: (teiUid: String, enrollmentUid: String?, isOnline: Boolean) -> Boolean,
    val onSyncClick: (String) -> Boolean
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<SearchTeiModel> {

    override fun bind(data: SearchTeiModel) {
        binding.overdue = data.isHasOverdue
        binding.isOnline = data.isOnline
        binding.setSyncState(data.tei.state())

        setEnrollment(data.enrollments)

        setTEIData(data.attributeValues)

        binding.trackedEntityImage.background =
            AppCompatResources.getDrawable(itemView.context, R.drawable.photo_temp_gray)
        binding.followUp.background =
            AppCompatResources.getDrawable(itemView.context, R.drawable.ic_circle_red)

        binding.syncState.setOnClickListener {
            if (data.tei.deleted() == true || data.selectedEnrollment != null && data.selectedEnrollment.deleted() == true) {
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

        val file = File(data.profilePicturePath)
        val placeHolderId = ObjectStyleUtils.getIconResource(
            itemView.context,
            data.defaultTypeIcon,
            R.drawable.photo_temp_gray
        )
        if (file.exists()) {
            Glide.with(itemView.context)
                .load(file)
                .placeholder(placeHolderId)
                .error(placeHolderId)
                .transition(withCrossFade())
                .transform(CircleCrop())
                .into(binding.trackedEntityImage)
        } else {
            binding.trackedEntityImage.setImageDrawable(placeHolderId)
        }
    }

    private fun setTEIData(trackedEntityAttributeValues: List<TrackedEntityAttributeValue>) {
        binding.attribute = trackedEntityAttributeValues
        binding.executePendingBindings()
    }

    private fun setEnrollment(enrollments: List<Enrollment>) {
        var isFollowUp = false
        for (enrollment in enrollments) {
            if (enrollment.followUp() != null && enrollment.followUp()!!) isFollowUp = true
        }
        binding.setFollowUp(isFollowUp)
    }
}