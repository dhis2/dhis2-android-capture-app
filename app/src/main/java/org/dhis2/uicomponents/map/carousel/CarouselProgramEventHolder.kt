package org.dhis2.uicomponents.map.carousel

import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import org.dhis2.R
import org.dhis2.databinding.ItemCarouselProgramEventBinding
import org.dhis2.usescases.programEventDetail.ProgramEventViewModel

class CarouselProgramEventHolder(
    val binding: ItemCarouselProgramEventBinding,
    val onClick: (eventUid: String?, orgUnitUid: String?) -> Boolean
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<ProgramEventViewModel> {

    override fun bind(data: ProgramEventViewModel) {
        binding.event = data
        itemView.setOnClickListener {
            onClick(data.uid(), data.orgUnitUid())
        }

        val stringBuilder = SpannableStringBuilder()
        data.eventDisplayData().forEachIndexed { index, nameValuePair ->
            val value = SpannableString(nameValuePair.val1())
            val colorToUse = if (index % 2 == 0) {
                Color.parseColor("#8A333333")
            } else {
                Color.parseColor("#61333333")
            }
            value.setSpan(
                ForegroundColorSpan(colorToUse),
                0,
                value.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(value)
            if (index != data.eventDisplayData().size - 1) {
                stringBuilder.append(" ")
            }
        }
        binding.dataValue.text = when {
            stringBuilder.isNotEmpty() -> stringBuilder
            else -> itemView.context.getString(R.string.no_data)
        }

        if (data.geometry() == null) {
            binding.noCoordinatesLabel.root.visibility = View.VISIBLE
            binding.noCoordinatesLabel.noCoordinatesMessage.text =
                itemView.context.getString(R.string.no_coordinates_item).format(
                    itemView.context.getString(R.string.event_event)
                        .toLowerCase(Locale.getDefault())
                )
        } else {
            binding.noCoordinatesLabel.root.visibility = View.INVISIBLE
        }
    }
}
