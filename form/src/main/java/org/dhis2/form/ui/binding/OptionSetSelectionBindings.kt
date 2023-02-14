package org.dhis2.form.ui.binding

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import org.dhis2.form.databinding.OptionSetSelectCheckItemBinding
import org.dhis2.form.databinding.OptionSetSelectItemBinding
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType

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
        UiRenderType.VERTICAL_CHECKBOXES -> {
            orientation = LinearLayout.VERTICAL
            visibility = View.VISIBLE
        }
        UiRenderType.HORIZONTAL_CHECKBOXES -> {
            orientation = LinearLayout.HORIZONTAL
            visibility = View.VISIBLE
        }
        else -> visibility = View.GONE
    }
}

@BindingAdapter("renderingType")
fun RadioGroup.setRenderingType(renderingType: UiRenderType?) {
    when (renderingType) {
        UiRenderType.VERTICAL_RADIOBUTTONS -> {
            orientation = LinearLayout.VERTICAL
            visibility = View.VISIBLE
        }
        UiRenderType.HORIZONTAL_RADIOBUTTONS -> {
            orientation = LinearLayout.HORIZONTAL
            visibility = View.VISIBLE
        }
        else -> visibility = View.GONE
    }
}

@BindingAdapter("options")
fun LinearLayout.addOptions(item: FieldUiModel) {
    if (item.renderingType?.isCheckBox() == true) {
        removeAllViews()
        item.optionSetConfiguration?.optionsToDisplay()?.forEach { option ->
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
        item.optionSetConfiguration?.optionsToDisplay()?.forEach { option ->
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
