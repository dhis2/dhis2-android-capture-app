package org.dhis2.uicomponents.map.carousel

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import org.dhis2.R
import org.dhis2.databinding.ItemCarouselProgramEventBinding
import org.dhis2.databinding.ItemFieldValueBinding
import org.dhis2.usescases.programEventDetail.ProgramEventViewModel

class CarouselProgramEventHolder(
    val binding: ItemCarouselProgramEventBinding,
    val onClick: (teiUid: String?, orgUnitUid: String?, eventUid: String?) -> Boolean,
    val attributeVisibilityCallback: (ProgramEventViewModel) -> Unit
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<ProgramEventViewModel> {

    override fun bind(data: ProgramEventViewModel) {
        with(data) {
            binding.event = this
            itemView.setOnClickListener {
                onClick(uid(), orgUnitUid(), uid())
            }

            when {
                eventDisplayData().isNotEmpty() -> setEventValueLayout(this) {
                    attributeVisibilityCallback(this)
                }
                else -> hideEventValueLayout()
            }

            when {
                geometry() == null -> {
                    binding.noCoordinatesLabel.root.visibility = View.VISIBLE
                    binding.noCoordinatesLabel.noCoordinatesMessage.text =
                        itemView.context.getString(R.string.no_coordinates_item).format(
                            itemView.context.getString(R.string.event_event)
                                .toLowerCase(Locale.getDefault())
                        )
                }
                else -> binding.noCoordinatesLabel.root.visibility = View.INVISIBLE
            }
        }
    }

    private fun setEventValueLayout(
        programEventModel: ProgramEventViewModel,
        toggleList: () -> Unit
    ) {
        binding.showValuesButton.visibility = View.VISIBLE
        binding.showValuesButton.setOnClickListener {
            toggleList.invoke()
        }
        initValues(programEventModel)
    }

    private fun hideEventValueLayout() {
        binding.showValuesButton.visibility = View.INVISIBLE
        binding.dataElementListGuideline.visibility = View.INVISIBLE
        binding.dataElementList.visibility = View.GONE
        binding.dataValue.text = itemView.context.getString(R.string.no_data)
        binding.showValuesButton.setOnClickListener(null)
    }

    private fun initValues(programEventModel: ProgramEventViewModel) {
        binding.dataElementList.removeAllViews()
        binding.dataValue.text = null
        binding.showValuesButton.scaleY = if (programEventModel.openedAttributeList) 1f else -1f
        binding.showValuesButton
            .animate()
            .scaleY(if (programEventModel.openedAttributeList) -1f else 1f)
            .setDuration(500)
            .withStartAction {
                binding.showValuesButton.scaleY =
                    if (programEventModel.openedAttributeList) 1f else -1f
            }
            .withEndAction {
                binding.showValuesButton.scaleY =
                    if (programEventModel.openedAttributeList) -1f else 1f
            }
            .start()
        if (programEventModel.openedAttributeList) {
            binding.dataElementListGuideline.visibility = View.VISIBLE
            binding.dataElementList.visibility = View.VISIBLE
            programEventModel.eventDisplayData().forEach { nameValuePair ->
                val fieldValueBinding: ItemFieldValueBinding =
                    ItemFieldValueBinding.inflate(
                        LayoutInflater.from(binding.dataElementList.context)
                    )
                fieldValueBinding.name = nameValuePair.first
                fieldValueBinding.value = nameValuePair.second
                binding.dataElementList.addView(fieldValueBinding.root)
            }
        } else {
            binding.dataElementListGuideline.visibility = View.INVISIBLE
            binding.dataElementList.visibility = View.GONE
            val stringBuilder = programEventModel.spannableStringValues
            when {
                stringBuilder.toString().isEmpty() -> hideEventValueLayout()
                else -> binding.dataValue.text = stringBuilder
            }
        }
    }
}
