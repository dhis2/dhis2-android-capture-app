package org.dhis2.form.ui.binding

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import org.dhis2.commons.bindings.clipWithAllRoundedCorners
import org.dhis2.commons.bindings.dp
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ObjectStyleUtils
import org.dhis2.form.R
import org.dhis2.form.databinding.FormImageBinding
import org.dhis2.form.databinding.FormImageMatrixBinding
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.option.Option

@BindingAdapter("optionImage")
fun ImageView.setOptionImage(option: Option) {
    val iconDrawable =
        ObjectStyleUtils.getIconResource(
            context,
            option.style().icon(),
            R.drawable.ic_default_icon,
            ColorUtils(),
        )
    val color =
        ObjectStyleUtils.getColorResource(context, option.style().color(), R.color.colorPrimary)
    iconDrawable?.let {
        setImageDrawable(ColorUtils().tintDrawableReosurce(iconDrawable, color))
    }
    setBackgroundColor(color)
    clipWithAllRoundedCorners(8.dp)
}

@BindingAdapter(value = ["optionSelectionModel", "optionSelectionOption"], requireAll = true)
fun View.setOptionSelection(field: FieldUiModel, option: Option) {
    val color =
        ObjectStyleUtils.getColorResource(context, option.style().color(), R.color.colorPrimary)
    val isSelected = field.displayName == option.displayName()
    when (this) {
        is MaterialCardView -> {
            clipWithAllRoundedCorners(8.dp)
            rippleColor = ColorStateList.valueOf(color)
            if (isSelected) {
                setCardBackgroundColor(ColorUtils().withAlpha(color, 50))
            } else {
                setBackgroundColor(Color.WHITE)
            }
        }
        is ImageView -> {
            setColorFilter(color)
            visibility = if (isSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}

@BindingAdapter("options")
fun FrameLayout.addOptions(item: FieldUiModel) {
    post {
        removeAllViews()
        val parentWidth = measuredWidth
        item.optionSetConfiguration?.optionsToDisplay()?.forEachIndexed { index, option ->
            val columns: Int
            val optionContainer = FrameLayout(context)
            val binding = when (item.renderingType) {
                UiRenderType.MATRIX ->
                    FormImageMatrixBinding.inflate(LayoutInflater.from(context), this, false)
                        .apply {
                            this.item = item
                            this.option = option
                        }.also { columns = 2 }
                else -> FormImageBinding.inflate(LayoutInflater.from(context), this, false).apply {
                    this.item = item
                    this.option = option
                }.also { columns = 1 }
            }
            optionContainer.apply {
                layoutParams = configLayoutParams(index, columns, parentWidth)
                addView(binding.root)
            }
            addView(optionContainer)
        }
    }
}
private fun configLayoutParams(index: Int, numberOfColumns: Int, parentWidth: Int) =
    FrameLayout.LayoutParams(
        getWidth(numberOfColumns, parentWidth),
        FrameLayout.LayoutParams.WRAP_CONTENT,
    ).apply {
        topMargin = getTopMargin(index, numberOfColumns)
        marginStart = getStartMargin(index, numberOfColumns, parentWidth)
        marginEnd = getEndMargin(index, numberOfColumns)
    }

private fun getWidth(numberOfColumns: Int, parentWidth: Int) = when (numberOfColumns) {
    2 -> (parentWidth / numberOfColumns) - 16.dp - 8.dp
    else -> FrameLayout.LayoutParams.MATCH_PARENT
}

private fun getTopMargin(index: Int, numberOfColumns: Int): Int {
    return when (numberOfColumns) {
        2 -> (index / numberOfColumns) * (190 + 16).dp
        else -> (index / numberOfColumns) * (96 + 16).dp
    }
}

private fun getStartMargin(index: Int, numberOfColumns: Int, parentWidth: Int): Int {
    return when (numberOfColumns) {
        2 -> when (index % numberOfColumns) {
            0 -> 16.dp
            else -> 8.dp + (parentWidth / numberOfColumns)
        }
        else -> 16.dp
    }
}

private fun getEndMargin(index: Int, numberOfColumns: Int): Int {
    return when (numberOfColumns) {
        2 -> when (index % numberOfColumns) {
            0 -> 8.dp
            else -> 16.dp
        }
        else -> 16.dp
    }
}
