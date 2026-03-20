package org.dhis2.usescases.searchTrackEntity.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.dhis2.commons.R
import org.dhis2.commons.bindings.getRemainingEnrollmentsForTei
import org.dhis2.commons.data.EnrollmentIconData
import org.dhis2.commons.databinding.ItemFieldValueBinding
import org.dhis2.commons.date.toUiText
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.commons.model.MetadataIconData
import org.dhis2.tracker.search.model.DomainEnrollment
import org.dhis2.tracker.search.model.DomainProgram
import org.dhis2.tracker.search.model.EnrollmentStatus
import org.dhis2.tracker.search.model.TrackedEntitySearchItemAttributeDomain
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel
import java.io.File
import java.util.Date

fun SearchTeiModel.setTeiImage(
    context: Context,
    teiImageView: ImageView,
    teiTextImageView: TextView,
    colorUtils: ColorUtils,
    pictureListener: (String) -> Unit,
) {
    val imageBg =
        AppCompatResources.getDrawable(
            context,
            R.drawable.photo_temp_gray,
        )
    imageBg?.colorFilter =
        PorterDuffColorFilter(
            colorUtils.getPrimaryColor(
                context,
                ColorType.PRIMARY,
            ),
            PorterDuff.Mode.SRC_IN,
        )
    teiImageView.background = imageBg
    val file = File(profilePicturePath)
    val placeHolderId =
        ResourceManager(context, colorUtils)
            .getObjectStyleDrawableResource(defaultTypeIcon, -1)
    teiImageView.setOnClickListener(null)
    when {
        file.exists() -> {
            teiTextImageView.visibility = View.GONE
            Glide
                .with(context)
                .load(file)
                .error(placeHolderId)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CircleCrop())
                .into(teiImageView)
            teiImageView.setOnClickListener { pictureListener(profilePicturePath) }
        }

        textAttributeValues != null &&
            textAttributeValues.values.isNotEmpty() &&
            ArrayList(textAttributeValues.values)[0].value != "-" -> {
            teiImageView.setImageDrawable(null)
            teiTextImageView.visibility = View.VISIBLE
            val valueToShow = ArrayList(textAttributeValues.values)
            if (valueToShow[0]?.value?.isEmpty() != false) {
                teiTextImageView.text = "?"
            } else {
                teiTextImageView.text =
                    valueToShow[0]
                        .value
                        ?.first()
                        .toString()
                        .uppercase()
            }
            teiTextImageView.setTextColor(
                colorUtils.getContrastColor(
                    colorUtils.getPrimaryColor(
                        context,
                        ColorType.PRIMARY,
                    ),
                ),
            )
        }

        tei.isOnline &&
            attributeValues.isNotEmpty() &&
            ArrayList(attributeValues.values).firstOrNull()?.value?.isNotEmpty() == true -> {
            teiImageView.setImageDrawable(null)
            teiTextImageView.visibility = View.VISIBLE
            val valueToShow = ArrayList(attributeValues.values)
            if (valueToShow[0] == null) {
                teiTextImageView.text = "?"
            } else {
                teiTextImageView.text =
                    valueToShow[0]
                        .value
                        ?.first()
                        .toString()
                        .uppercase()
            }
            teiTextImageView.setTextColor(
                colorUtils.getContrastColor(
                    colorUtils.getPrimaryColor(
                        context,
                        ColorType.PRIMARY,
                    ),
                ),
            )
        }

        placeHolderId != -1 -> {
            teiTextImageView.visibility = View.GONE
            val icon =
                AppCompatResources.getDrawable(
                    context,
                    placeHolderId,
                )
            icon?.colorFilter =
                PorterDuffColorFilter(
                    colorUtils.getContrastColor(
                        colorUtils.getPrimaryColor(
                            context,
                            ColorType.PRIMARY,
                        ),
                    ),
                    PorterDuff.Mode.SRC_IN,
                )
            teiImageView.setImageDrawable(icon)
        }

        else -> {
            teiImageView.setImageDrawable(null)
            teiTextImageView.visibility = View.VISIBLE
            teiTextImageView.text = "?"
            teiTextImageView.setTextColor(
                colorUtils.getContrastColor(
                    colorUtils.getPrimaryColor(
                        context,
                        ColorType.PRIMARY,
                    ),
                ),
            )
        }
    }
}

fun LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain>.setAttributeList(
    parentLayout: LinearLayout,
    showAttributesButton: ImageView,
    adapterPosition: Int,
    listIsOpen: Boolean,
    sortingKey: String?,
    sortingValue: String?,
    orgUnit: String?,
    showList: () -> Unit,
) {
    parentLayout.removeAllViews()
    if (size > 3) {
        for (pos in 1 until size) {
            val fieldName =
                keys.toTypedArray()[pos]
            val fieldValue = this[fieldName]?.value
            val itemFieldValueBinding =
                ItemFieldValueBinding.inflate(LayoutInflater.from(parentLayout.context))
            itemFieldValueBinding.name = fieldName
            itemFieldValueBinding.value = fieldValue
            itemFieldValueBinding.root.tag = adapterPosition.toString() + "_" + fieldName
            parentLayout.addView(itemFieldValueBinding.root)
        }
        val orgUnitKey = parentLayout.context.getString(R.string.enrolled_in)
        val itemFieldValueBinding =
            ItemFieldValueBinding.inflate(LayoutInflater.from(parentLayout.context))
        itemFieldValueBinding.name = orgUnitKey
        itemFieldValueBinding.value = orgUnit
        itemFieldValueBinding.root.tag = adapterPosition.toString() + "_" + orgUnitKey
        parentLayout.addView(itemFieldValueBinding.root)
        if (sortingKey != null) {
            val binding =
                ItemFieldValueBinding.inflate(LayoutInflater.from(parentLayout.context))
            binding.name = sortingKey
            binding.fieldName.setTextColor(
                ResourcesCompat.getColor(
                    binding.fieldName.context.resources,
                    R.color.sorting_attribute_key_color,
                    null,
                ),
            )
            binding.value = sortingValue
            binding.fieldValue.setTextColor(
                ResourcesCompat.getColor(
                    binding.fieldValue.context.resources,
                    R.color.sorting_attribute_value_color,
                    null,
                ),
            )
            binding.root.tag = adapterPosition.toString() + "_" + sortingValue
            parentLayout.addView(binding.root)
        }
        showAttributesButton.scaleY = if (listIsOpen) -1F else 1F
        showAttributesButton.setOnClickListener {
            showList()
        }
    } else {
        showAttributesButton.setOnClickListener(null)
    }
}



fun DomainEnrollment.setStatusText(
    context: Context,
    statusTextView: TextView,
    isOverdue: Boolean,
    dueDate: Date?,
) {
    var textToShow: String? = null
    var color = -1
    when {
        isOverdue -> {
            textToShow = dueDate.toUiText(context)
            color = "#E91E63".toColorInt()
        }

        status == EnrollmentStatus.CANCELLED -> {
            textToShow = context.getString(R.string.cancelled)
            color = "#E91E63".toColorInt()
        }

        status == EnrollmentStatus.COMPLETED -> {
            textToShow = context.getString(R.string.enrollment_status_completed)
            color = "#8A333333".toColorInt()
        }
    }
    statusTextView.visibility = if (textToShow == null) View.GONE else View.VISIBLE
    statusTextView.text = textToShow
    statusTextView.setTextColor(color)
    val bgDrawable =
        AppCompatResources.getDrawable(
            context,
            R.drawable.round_border_box_2,
        ) as GradientDrawable?
    bgDrawable!!.setStroke(2, color)
    statusTextView.background = bgDrawable
}


fun List<DomainEnrollment>.hasFollowUp(): Boolean =
    firstOrNull { enrollment ->
        enrollment.followUp
    }?.followUp ?: false

fun List<DomainProgram>.getEnrollmentIconsData(
    currentProgram: String?,
    provideMetadataIconData: (String) -> MetadataIconData,
): List<EnrollmentIconData> {
    val enrollmentIconDataList: MutableList<EnrollmentIconData> = mutableListOf()

    val filteredList = this.filter { it.uid != currentProgram }
    this.filter { it.uid != currentProgram }
        .forEachIndexed { index, program ->
            filteredList.size.let {
                if (it <= 4) {
                    enrollmentIconDataList.add(
                        EnrollmentIconData(0, 0, true, 0, provideMetadataIconData( program.uid)),
                    )
                } else {
                    if (index in 0..2) {
                        enrollmentIconDataList.add(
                            EnrollmentIconData(0, 0, true, 0, provideMetadataIconData(program.uid)),
                        )
                    }
                    if (index == 3) {
                        //TODO check default size
                        enrollmentIconDataList.add(
                            EnrollmentIconData(
                                0,
                                0,
                                false,
                                getRemainingEnrollmentsForTei(filteredList.size),
                                provideMetadataIconData(program.uid),
                            ),
                        )
                    }
                }
            }
        }
    return enrollmentIconDataList
}

