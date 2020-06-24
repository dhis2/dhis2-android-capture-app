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

class CarouselEventHolder(
    val binding: ItemCarouselTeiBinding
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<SearchTeiModel> {

    override fun bind(data: SearchTeiModel) {

    }
}