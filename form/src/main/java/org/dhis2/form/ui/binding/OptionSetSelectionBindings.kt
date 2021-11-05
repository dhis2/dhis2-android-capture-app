package org.dhis2.form.ui.binding

import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import org.dhis2.form.databinding.OptionSetSelectCheckItemBinding
import org.dhis2.form.databinding.OptionSetSelectItemBinding
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.style.FormUiColorType
import org.dhis2.form.ui.style.FormUiModelStyle

@BindingAdapter("delete_visibility")
fun ImageView.setOptionSetDeleteVisibility(item: FieldUiModel) {
    visibility = when {
        item.value != null && item.editable -> when (item.renderingType) {
            UiRenderType.HORIZONTAL_RADIOBUTTONS,
            UiRenderType.VERTICAL_RADIOBUTTONS -> View.VISIBLE
            else -> View.GONE
        }
        else -> View.GONE
    }
}

@BindingAdapter("renderingType")
fun LinearLayout.setRenderingType(renderingType: UiRenderType?) {
    when (renderingType) {
        UiRenderType.VERTICAL_CHECKBOXES -> orientation = LinearLayout.VERTICAL
        UiRenderType.HORIZONTAL_CHECKBOXES -> orientation = LinearLayout.HORIZONTAL
        else -> visibility = View.GONE
    }
}

@BindingAdapter("renderingType")
fun RadioGroup.setRenderingType(renderingType: UiRenderType?) {
    when (renderingType) {
        UiRenderType.VERTICAL_RADIOBUTTONS -> orientation = LinearLayout.VERTICAL
        UiRenderType.HORIZONTAL_RADIOBUTTONS -> orientation = LinearLayout.HORIZONTAL
        else -> visibility = View.GONE
    }
}

@BindingAdapter("options")
fun LinearLayout.addOptions(item: FieldUiModel) {
    if (item.renderingType?.isCheckBox() == true) {
        removeAllViews()
        item.options?.filter { item.canShowOption(it.uid()) }?.forEach { option ->
            val optionBinding: OptionSetSelectCheckItemBinding =
                OptionSetSelectCheckItemBinding.inflate(LayoutInflater.from(context), this, false)
            optionBinding.apply {
                this.item = item
                this.option = option
                checkBox.apply {
                    val optionChecked = item.displayName == option.displayName()
                    isChecked = optionChecked
                    setOnCheckedChangeListener { _, isChecked ->
                        when {
                            isChecked -> item.onSave(option.code())
                            else -> item.takeIf { optionChecked }?.onClear()
                        }
                    }
                }
            }
            addView(optionBinding.root)
        }
    }
}

@BindingAdapter("options")
fun RadioGroup.addOptions(item: FieldUiModel) {
    if (item.renderingType?.isRadioButton() == true) {
        removeAllViews()
        item.options?.filter { item.canShowOption(it.uid()) }?.forEach { option ->
            val optionBinding: OptionSetSelectItemBinding =
                OptionSetSelectItemBinding.inflate(LayoutInflater.from(context), this, false)
            optionBinding.apply {
                this.item = item
                this.option = option
                radio.apply {
                    isChecked = item.displayName == option.displayName()
                    setOnCheckedChangeListener { _, isChecked ->
                        when {
                            isChecked -> item.onSave(option.code())
                        }
                    }
                }
            }
            addView(optionBinding.root)
        }
    }
}

private fun FieldUiModel.canShowOption(optionUid: String): Boolean {
    val inOptionsToShow: Boolean = optionsToShow?.contains(optionUid) ?: false
    val inOptionsToHide: Boolean = optionsToHide?.contains(optionUid) ?: false
    return when {
        optionsToShow?.isNotEmpty() ?: false -> inOptionsToShow
        else -> !inOptionsToHide
    }
}

@BindingAdapter("optionTint")
fun CompoundButton.setOptionTint(style: FormUiModelStyle?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        style?.let {
            it.getColors()[FormUiColorType.PRIMARY]?.let { primaryColor ->
                it.getColors()[FormUiColorType.TEXT_PRIMARY]?.let { textPrimaryColor ->
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        ),
                        intArrayOf(
                            primaryColor,
                            textPrimaryColor
                        )
                    )
                    buttonTintList = colorStateList
                    setTextColor(colorStateList)
                }
            }
        }
    }
}
