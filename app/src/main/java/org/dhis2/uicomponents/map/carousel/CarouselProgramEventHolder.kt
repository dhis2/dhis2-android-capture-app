package org.dhis2.uicomponents.map.carousel

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import org.dhis2.R
import org.dhis2.data.tuples.Pair
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

        val attributesString = SpannableStringBuilder("")
        data.eventDisplayData().forEach {
            attributesString.append(setAttributes(it))
        }
        binding.dataValue.text = when {
            attributesString.isNotEmpty() -> attributesString
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

    private fun setAttributes(attribute: Pair<String, String>): SpannableStringBuilder {
        val attributeValue = if (attribute.val1().isNullOrEmpty()) {
            "-"
        } else {
            attribute.val1()
        }
        return SpannableStringBuilder("${attribute.val0()} $attributeValue  ").apply {
            setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(itemView.context, R.color.text_black_8A3)
                ),
                0, attribute.val0().length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}
