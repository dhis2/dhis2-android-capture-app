package org.dhis2.Bindings

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.io.File
import java.util.ArrayList
import java.util.Date
import org.dhis2.R
import org.dhis2.databinding.ItemFieldValueBinding
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import timber.log.Timber

fun List<Enrollment>.hasFollowUp(): Boolean {
    return firstOrNull { enrollment ->
        enrollment.followUp() == true
    }?.let {
        it.followUp() == true
    } ?: false
}

fun List<Program>.addEnrollmentIcons(context: Context, parent: ViewGroup, currentProgram: String?) {
    parent.removeAllViews()
    filter { it.uid() != currentProgram }
        .forEach { program ->
            val color = ColorUtils.getColorFrom(
                program.style().color(),
                ColorUtils.getPrimaryColor(
                    context,
                    ColorUtils.ColorType.PRIMARY
                )
            )
            val imageResource =
                ResourceManager(context)
                    .getObjectStyleDrawableResource(
                        program.style().icon(),
                        -1
                    )
            val imageView =
                ImageView(context)
            val params = LinearLayout.LayoutParams(24.dp, 24.dp)
            params.marginEnd = 4
            imageView.layoutParams = params
            imageView.setImageDrawable(getProgramDrawable(context, color, imageResource))
            imageView.setPadding(0, 0, 0, 0)
            parent.addView(imageView)
        }
}

private fun getProgramDrawable(context: Context, color: Int, icon: Int): Drawable? {
    var iconImage: Drawable?
    try {
        iconImage = AppCompatResources.getDrawable(
            context,
            icon
        )
        iconImage!!.mutate()
    } catch (e: Exception) {
        Timber.log(1, e)
        iconImage = AppCompatResources.getDrawable(
            context,
            R.drawable.ic_program_default
        )
        iconImage!!.mutate()
    }
    val bgDrawable = AppCompatResources.getDrawable(
        context,
        R.drawable.rounded_square_r2_24
    )
    val wrappedIcon = DrawableCompat.wrap(iconImage!!)
    val wrappedBg = DrawableCompat.wrap(bgDrawable!!)
    val finalDrawable = LayerDrawable(arrayOf(wrappedBg, wrappedIcon))
    finalDrawable.mutate()
    finalDrawable.getDrawable(1).colorFilter = PorterDuffColorFilter(
        ColorUtils.getContrastColor(color),
        PorterDuff.Mode.SRC_IN
    )
    finalDrawable.getDrawable(0).colorFilter =
        PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    return finalDrawable
}

fun Enrollment.setStatusText(
    context: Context,
    statusTextView: TextView,
    isOverdue: Boolean,
    dueDate: Date?
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
            textToShow = context.getString(R.string.completed)
            color = Color.parseColor("#8A333333")
        }
    }
    statusTextView.visibility = if (textToShow == null) View.GONE else View.VISIBLE
    statusTextView.text = textToShow
    statusTextView.setTextColor(color)
    val bgDrawable =
        AppCompatResources.getDrawable(
            context,
            R.drawable.round_border_box_2
        ) as GradientDrawable?
    bgDrawable!!.setStroke(2, color)
    statusTextView.background = bgDrawable
}

fun SearchTeiModel.setTeiImage(
    context: Context,
    teiImageView: ImageView,
    teiTextImageView: TextView
) {
    val imageBg = AppCompatResources.getDrawable(
        context,
        R.drawable.photo_temp_gray
    )
    imageBg!!.colorFilter = PorterDuffColorFilter(
        ColorUtils.getPrimaryColor(
            context,
            ColorUtils.ColorType.PRIMARY
        ),
        PorterDuff.Mode.SRC_IN
    )
    teiImageView.background = imageBg
    val file = File(profilePicturePath)
    val placeHolderId = ResourceManager(context)
        .getObjectStyleDrawableResource(defaultTypeIcon, -1)
    if (file.exists()) {
        teiTextImageView.visibility = View.GONE
        Glide.with(context)
            .load(file)
            .placeholder(placeHolderId)
            .error(placeHolderId)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(teiImageView)
    } else if (placeHolderId != -1) {
        teiTextImageView.visibility = View.GONE
        teiImageView.setImageResource(placeHolderId)
    } else if (attributeValues != null &&
        attributeValues.values.isNotEmpty()
    ) {
        teiImageView.setImageDrawable(null)
        teiTextImageView.visibility = View.VISIBLE
        val valueToShow = ArrayList(attributeValues.values)
        if (valueToShow[0] == null) {
            teiTextImageView.text = "-"
        } else {
            teiTextImageView.text = valueToShow[0].value().toString()
        }
        teiTextImageView.setTextColor(
            ColorUtils.getContrastColor(
                ColorUtils.getPrimaryColor(
                    context,
                    ColorUtils.ColorType.PRIMARY
                )
            )
        )
    } else {
        teiTextImageView.visibility = View.GONE
    }
}

fun LinkedHashMap<String, TrackedEntityAttributeValue>.setAttributeList(
    parentLayout: LinearLayout,
    showAttributesButton: ImageView,
    adapterPosition: Int,
    showList: (Boolean) -> Unit
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
        showAttributesButton.setOnClickListener {
            showAttributesButton.animate()
                .scaleY(if (parentLayout.visibility == View.VISIBLE) 1F else -1F)
                .setDuration(200).start()
            val shouldShowAttributeList =
                parentLayout.visibility == View.GONE
            showList(shouldShowAttributeList)
        }
    } else {
        showAttributesButton.setOnClickListener(null)
    }
}
