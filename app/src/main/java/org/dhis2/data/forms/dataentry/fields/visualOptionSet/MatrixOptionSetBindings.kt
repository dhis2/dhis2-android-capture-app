package org.dhis2.data.forms.dataentry.fields.visualOptionSet

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import org.dhis2.Bindings.clipWithAllRoundedCorners
import org.dhis2.R
import org.dhis2.commons.bindings.dp
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.utils.ObjectStyleUtils
import org.hisp.dhis.android.core.option.Option

@BindingAdapter("optionImage")
fun ImageView.setOptionImage(option: Option) {
    val iconDrawable =
        ObjectStyleUtils.getIconResource(context, option.style().icon(), R.drawable.ic_default_icon)
    val color =
        ObjectStyleUtils.getColorResource(context, option.style().color(), R.color.colorPrimary)
    setImageDrawable(
        ColorUtils.tintDrawableReosurce(iconDrawable, color)
    )
    setBackgroundColor(color)
    clipWithAllRoundedCorners(8.dp)
}

@BindingAdapter(value = ["optionSelectionModel", "optionSelectionOption"], requireAll = true)
fun View.setOptionSelection(matrixOptionSetModel: MatrixOptionSetModel, option: Option) {
    val color =
        ObjectStyleUtils.getColorResource(context, option.style().color(), R.color.colorPrimary)
    val isSelected = matrixOptionSetModel.isSelected(option)
    when (this) {
        is MaterialCardView -> {
            clipWithAllRoundedCorners(8.dp)
            rippleColor = ColorStateList.valueOf(color)
            if (isSelected) {
                setCardBackgroundColor(ColorUtils.withAlpha(color, 50))
            } else {
                setBackgroundColor(Color.WHITE)
            }
        }
        is ImageView -> {
            setColorFilter(color)
            visibility = if (matrixOptionSetModel.isSelected(option)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}
