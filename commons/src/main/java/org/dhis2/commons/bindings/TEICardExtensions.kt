package org.dhis2.commons.bindings

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import org.dhis2.commons.R
import org.dhis2.commons.data.EnrollmentIconData
import org.dhis2.commons.databinding.ItemFieldValueBinding
import org.dhis2.commons.date.toUiText
import org.dhis2.mobile.commons.extensions.toColor
import org.dhis2.mobile.commons.model.MetadataIconData
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatar
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatarSize
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataIcon
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import java.util.Date

const val ENROLLMENT_ICONS_TO_SHOW = 3
const val MAX_NUMBER_REMAINING_ENROLLMENTS = 99

fun List<Enrollment>.hasFollowUp(): Boolean =
    firstOrNull { enrollment ->
        enrollment.followUp() == true
    }?.let {
        it.followUp() == true
    } ?: false

fun List<Program>.getEnrollmentIconsData(
    currentProgram: String?,
    provideMetadataIconData: (String) -> MetadataIconData,
): List<EnrollmentIconData> {
    val enrollmentIconDataList: MutableList<EnrollmentIconData> = mutableListOf()

    val filteredList = filter { it.uid() != currentProgram }
    this
        .filter { it.uid() != currentProgram }
        .forEachIndexed { index, program ->
            if (filteredList.size <= 4) {
                enrollmentIconDataList.add(
                    EnrollmentIconData(0, 0, true, 0, provideMetadataIconData(program.uid())),
                )
            } else {
                if (index in 0..2) {
                    enrollmentIconDataList.add(
                        EnrollmentIconData(0, 0, true, 0, provideMetadataIconData(program.uid())),
                    )
                }
                if (index == 3) {
                    enrollmentIconDataList.add(
                        EnrollmentIconData(
                            0,
                            0,
                            false,
                            getRemainingEnrollmentsForTei(filteredList.size),
                            provideMetadataIconData(program.uid()),
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
            DHIS2Theme {
                Row(
                    horizontalArrangement = spacedBy(Dp(4f)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    forEach { enrollmentIcon ->
                        if (enrollmentIcon.isIcon) {
                            MetadataAvatar(
                                modifier =
                                    Modifier
                                        .size(56.dp)
                                        .alpha(0.5f),
                                icon = {
                                    if (enrollmentIcon.metadataIconData.isFileLoaded()) {
                                        MetadataIcon(
                                            imageCardData = enrollmentIcon.metadataIconData.imageCardData,
                                        )
                                    } else {
                                        Icon(
                                            painter = provideDHIS2Icon("dhis2_image_not_supported"),
                                            contentDescription = "",
                                        )
                                    }
                                },
                                iconTint = enrollmentIcon.metadataIconData.color,
                                size = MetadataAvatarSize.M(),
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

@Composable
private fun SquareWithNumber(number: Int) {
    Box(
        modifier =
            Modifier
                .size(25.dp)
                .clip(RoundedCornerShape(4.dp))
                .background("#f2f2f2".toColor()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+$number",
            color = "#6f6f6f".toColor(),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(org.dhis2.ui.R.font.rubik_regular)),
        )
    }
}

fun getRemainingEnrollmentsForTei(teiEnrollmentCount: Int): Int =
    if (teiEnrollmentCount - ENROLLMENT_ICONS_TO_SHOW > MAX_NUMBER_REMAINING_ENROLLMENTS) {
        MAX_NUMBER_REMAINING_ENROLLMENTS
    } else {
        teiEnrollmentCount - ENROLLMENT_ICONS_TO_SHOW
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
            color = "#E91E63".toColorInt()
        }

        status() == EnrollmentStatus.CANCELLED -> {
            textToShow = context.getString(R.string.cancelled)
            color = "#E91E63".toColorInt()
        }

        status() == EnrollmentStatus.COMPLETED -> {
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
