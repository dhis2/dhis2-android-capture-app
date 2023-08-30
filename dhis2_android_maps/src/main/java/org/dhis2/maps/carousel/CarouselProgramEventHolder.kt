package org.dhis2.maps.carousel

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.animations.collapse
import org.dhis2.commons.animations.expand
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.databinding.ItemFieldValueBinding
import org.dhis2.maps.R
import org.dhis2.maps.databinding.ItemCarouselProgramEventBinding

class CarouselProgramEventHolder(
    val binding: ItemCarouselProgramEventBinding,
    val onClick: (teiUid: String?, orgUnitUid: String?, eventUid: String?) -> Boolean,
    val attributeVisibilityCallback: (ProgramEventViewModel) -> Unit,
    val onNavigate: (teiUid: String) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root),
    CarouselBinder<ProgramEventViewModel> {

    override fun bind(data: ProgramEventViewModel) {
        with(data) {
            binding.event = this
            binding.eventInfoCard.setOnClickListener {
                onClick(uid(), orgUnitUid(), uid())
            }

            when {
                eventDisplayData().isNotEmpty() -> setEventValueLayout(this) {
                    attributeVisibilityCallback(this)
                }
                else -> hideEventValueLayout()
            }

            binding.mapNavigateFab.setOnClickListener {
                onNavigate(data.uid())
            }
        }
    }

    private fun setEventValueLayout(
        programEventModel: ProgramEventViewModel,
        toggleList: () -> Unit,
    ) {
        binding.showValuesButtonContainer.visibility = View.VISIBLE
        binding.showValuesButtonContainer.setOnClickListener {
            if (programEventModel.openedAttributeList) {
                binding.dataElementList.collapse {
                    initValues(false, programEventModel.eventDisplayData())
                }
            } else {
                binding.dataElementList.expand {
                    initValues(true, programEventModel.eventDisplayData())
                }
            }
            toggleList.invoke()
        }
        initValues(programEventModel.openedAttributeList, programEventModel.eventDisplayData())
    }

    private fun hideEventValueLayout() {
        binding.showValuesButtonContainer.visibility = View.INVISIBLE
        binding.dataElementListGuideline.visibility = View.INVISIBLE
        binding.dataElementList.visibility = View.GONE
        binding.dataValue.text = itemView.context.getString(R.string.no_data)
        binding.showValuesButtonContainer.setOnClickListener(null)
    }

    private fun initValues(
        valueListIsOpen: Boolean,
        dataElementValues: MutableList<Pair<String, String>>,
    ) {
        binding.dataElementList.removeAllViews()
        binding.dataValue.text = null
        binding.showValuesButton.scaleY = if (valueListIsOpen) 1f else -1f
        binding.showValuesButton
            .animate()
            .scaleY(if (valueListIsOpen) -1f else 1f)
            .setDuration(500)
            .withStartAction { binding.showValuesButton.scaleY = if (valueListIsOpen) 1f else -1f }
            .withEndAction { binding.showValuesButton.scaleY = if (valueListIsOpen) -1f else 1f }
            .start()
        if (valueListIsOpen) {
            binding.dataElementListGuideline.visibility = View.VISIBLE
            binding.dataElementList.visibility = View.VISIBLE
            for (nameValuePair in dataElementValues) {
                val fieldValueBinding: ItemFieldValueBinding =
                    ItemFieldValueBinding.inflate(
                        LayoutInflater.from(binding.dataElementList.context),
                    )
                fieldValueBinding.name = nameValuePair.val0()
                fieldValueBinding.value = nameValuePair.val1()
                binding.dataElementList.addView(fieldValueBinding.root)
            }
        } else {
            binding.dataElementListGuideline.visibility = View.INVISIBLE
            binding.dataElementList.visibility = View.GONE
            val stringBuilder =
                SpannableStringBuilder()
            for (nameValuePair in dataElementValues) {
                if (nameValuePair.val1() != "-") {
                    val value =
                        SpannableString(nameValuePair.val1())
                    val colorToUse =
                        if (dataElementValues.indexOf(nameValuePair) % 2 == 0) {
                            ContextCompat.getColor(itemView.context, R.color.textPrimary)
                        } else {
                            ContextCompat.getColor(itemView.context, R.color.secondaryColor)
                        }
                    value.setSpan(
                        ForegroundColorSpan(colorToUse),
                        0,
                        value.length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE,
                    )
                    stringBuilder.append(value)
                    if (dataElementValues.indexOf(nameValuePair) != dataElementValues.size - 1) {
                        stringBuilder.append(" ")
                    }
                }
            }
            when {
                stringBuilder.toString().isEmpty() -> hideEventValueLayout()
                else -> binding.dataValue.text = stringBuilder
            }
        }
    }

    override fun showNavigateButton() {
        binding.mapNavigateFab.show()
    }

    override fun hideNavigateButton() {
        binding.mapNavigateFab.hide()
    }
}
