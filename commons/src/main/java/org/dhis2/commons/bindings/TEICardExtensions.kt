package org.dhis2.bindings

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.commons.R
import org.dhis2.commons.data.EnrollmentIconData
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.databinding.ItemFieldValueBinding
import org.dhis2.commons.date.toUiText
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.ui.MetadataIcon
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.SquareWithNumber
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import timber.log.Timber
import java.io.File
import java.util.Date

const val ENROLLMENT_ICONS_TO_SHOW = 3
const val MAX_NUMBER_REMAINING_ENROLLMENTS = 99

fun List<Enrollment>.hasFollowUp(): Boolean {
    return firstOrNull { enrollment ->
        enrollment.followUp() == true
    }?.let {
        it.followUp() == true
    } ?: false
}

fun List<Program>.getEnrollmentIconsData(
    currentProgram: String?,
    metadataIconData: MetadataIconData,
): List<EnrollmentIconData> {
    val enrollmentIconDataList: MutableList<EnrollmentIconData> = mutableListOf()

    val filteredList = filter { it.uid() != currentProgram }
    this.filter { it.uid() != currentProgram }
        .forEachIndexed { index, program ->
            if (filteredList.size <= 4) {
                enrollmentIconDataList.add(EnrollmentIconData(0, 0, true, 0, metadataIconData))
            } else {
                if (index in 0..2) {
                    enrollmentIconDataList.add(EnrollmentIconData(0, 0, true, 0, metadataIconData))
                }
                if (index == 3) {
                    enrollmentIconDataList.add(
                        EnrollmentIconData(
                            0,
                            0,
                            false,
                            getRemainingEnrollmentsForTei(filteredList.size),
                            metadataIconData,
                        ),
                    )
                }
            }
        }
    return enrollmentIconDataList
}

fun List<EnrollmentIconData>.paintAllEnrollmentIcons(parent: ComposeView) {
    parent.apply {
        setContent {
            MdcTheme {
                Row(
                    horizontalArrangement = spacedBy(Dp(4f)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    forEach { enrollmentIcon ->
                        if (enrollmentIcon.isIcon) {
                            MetadataIcon(
                                metadataIconData = enrollmentIcon.metadataIconData,
                            )
                        } else {
                            SquareWithNumber(enrollmentIcon.remainingEnrollments)
                        }
                    }
                }
            }
        }
    }
}

fun getRemainingEnrollmentsForTei(teiEnrollmentCount: Int): Int {
    return if (teiEnrollmentCount - ENROLLMENT_ICONS_TO_SHOW > MAX_NUMBER_REMAINING_ENROLLMENTS) {
        MAX_NUMBER_REMAINING_ENROLLMENTS
    } else {
        teiEnrollmentCount - ENROLLMENT_ICONS_TO_SHOW
    }
}

private fun getProgramDrawable(
    context: Context,
    color: Int,
    icon: Int,
    colorUtils: ColorUtils,
): Drawable? {
    var iconImage: Drawable?
    try {
        iconImage = AppCompatResources.getDrawable(
            context,
            icon,
        )
        iconImage!!.mutate()
    } catch (e: Exception) {
        Timber.log(1, e)
        iconImage = AppCompatResources.getDrawable(
            context,
            R.drawable.ic_default_outline,
        )
        iconImage!!.mutate()
    }
    val bgDrawable = AppCompatResources.getDrawable(
        context,
        R.drawable.rounded_square_r2_24,
    )
    val wrappedIcon = DrawableCompat.wrap(iconImage!!)
    val wrappedBg = DrawableCompat.wrap(bgDrawable!!)
    val finalDrawable = LayerDrawable(arrayOf(wrappedBg, wrappedIcon))
    finalDrawable.mutate()
    finalDrawable.getDrawable(1).colorFilter = PorterDuffColorFilter(
        colorUtils.getContrastColor(color),
        PorterDuff.Mode.SRC_IN,
    )
    finalDrawable.getDrawable(0).colorFilter =
        PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    return finalDrawable
}

fun Enrollment.setStatusText(
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
            color = Color.parseColor("#E91E63")
        }

        status() == EnrollmentStatus.CANCELLED -> {
            textToShow = context.getString(R.string.cancelled)
            color = Color.parseColor("#E91E63")
        }

        status() == EnrollmentStatus.COMPLETED -> {
            textToShow = context.getString(R.string.enrollment_status_completed)
            color = Color.parseColor("#8A333333")
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

fun SearchTeiModel.setTeiImage(
    context: Context,
    teiImageView: ImageView,
    teiTextImageView: TextView,
    colorUtils: ColorUtils,
    pictureListener: (String) -> Unit,
) {
    val imageBg = AppCompatResources.getDrawable(
        context,
        R.drawable.photo_temp_gray,
    )
    imageBg!!.colorFilter = PorterDuffColorFilter(
        colorUtils.getPrimaryColor(
            context,
            ColorType.PRIMARY,
        ),
        PorterDuff.Mode.SRC_IN,
    )
    teiImageView.background = imageBg
    val file = File(profilePicturePath)
    val placeHolderId = ResourceManager(context, colorUtils)
        .getObjectStyleDrawableResource(defaultTypeIcon, -1)
    teiImageView.setOnClickListener(null)
    if (file.exists()) {
        teiTextImageView.visibility = View.GONE
        Glide.with(context)
            .load(file)
            .error(placeHolderId)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(teiImageView)
        teiImageView.setOnClickListener { pictureListener(profilePicturePath) }
    } else if (textAttributeValues != null &&
        textAttributeValues.values.isNotEmpty() &&
        ArrayList(textAttributeValues.values)[0].value() != "-"
    ) {
        teiImageView.setImageDrawable(null)
        teiTextImageView.visibility = View.VISIBLE
        val valueToShow = ArrayList(textAttributeValues.values)
        if (valueToShow[0]?.value()?.isEmpty() != false) {
            teiTextImageView.text = "?"
        } else {
            teiTextImageView.text = valueToShow[0].value()?.first().toString().toUpperCase()
        }
        teiTextImageView.setTextColor(
            colorUtils.getContrastColor(
                colorUtils.getPrimaryColor(
                    context,
                    ColorType.PRIMARY,
                ),
            ),
        )
    } else if (isOnline && attributeValues.isNotEmpty() &&
        !ArrayList(attributeValues.values).first().value().isNullOrEmpty()
    ) {
        teiImageView.setImageDrawable(null)
        teiTextImageView.visibility = View.VISIBLE
        val valueToShow = ArrayList(attributeValues.values)
        if (valueToShow[0] == null) {
            teiTextImageView.text = "?"
        } else {
            teiTextImageView.text = valueToShow[0].value()?.first().toString().toUpperCase()
        }
        teiTextImageView.setTextColor(
            colorUtils.getContrastColor(
                colorUtils.getPrimaryColor(
                    context,
                    ColorType.PRIMARY,
                ),
            ),
        )
    } else if (placeHolderId != -1) {
        teiTextImageView.visibility = View.GONE
        val icon = AppCompatResources.getDrawable(
            context,
            placeHolderId,
        )
        icon!!.colorFilter = PorterDuffColorFilter(
            colorUtils.getContrastColor(
                colorUtils.getPrimaryColor(
                    context,
                    ColorType.PRIMARY,
                ),
            ),
            PorterDuff.Mode.SRC_IN,
        )
        teiImageView.setImageDrawable(icon)
    } else {
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

fun LinkedHashMap<String, TrackedEntityAttributeValue>.setAttributeList(
    parentLayout: LinearLayout,
    showAttributesButton: ImageView,
    adapterPosition: Int,
    listIsOpen: Boolean,
    sortingKey: String?,
    sortingValue: String?,
    orgUnit: String,
    showList: () -> Unit,
) {
    parentLayout.removeAllViews()
    if (size > 3) {
        for (pos in 1 until size) {
            val fieldName =
                keys.toTypedArray()[pos]
            val fieldValue = this[fieldName]?.value()
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
