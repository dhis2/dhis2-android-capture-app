package org.dhis2.form.ui.binding

import android.content.res.ColorStateList
import android.os.Build
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.KeyboardActionType
import org.dhis2.form.ui.style.FormUiColorType
import org.dhis2.form.ui.style.FormUiModelStyle
import org.hisp.dhis.android.core.common.ValueType

@BindingAdapter("label_text_color")
fun TextView.setLabelTextColor(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.FIELD_LABEL_TEXT]?.let { color ->
            setTextColor(color)
        }
    }
}

@BindingAdapter("icon_color")
fun ImageView.setIconColor(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.FIELD_LABEL_TEXT]?.let { color ->
            setColorFilter(color)
        }
    }
}

@BindingAdapter("description_icon_tint")
fun ImageView.tintDescriptionIcon(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.PRIMARY]?.let { color ->
            setColorFilter(color)
        }
    }
}

@BindingAdapter("action_icon_tint")
fun ImageView.tintActionIcon(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.ACTION_ICON]?.let { color ->
            setColorFilter(color)
        }
    }
}

@BindingAdapter("input_style")
fun TextView.setInputStyle(styleItem: FieldUiModel?) {
    styleItem?.let { uiModel ->
        uiModel.textColor?.let {
            setTextColor(it)
        }
        uiModel.backGroundColor?.let {
            ViewCompat.setBackgroundTintList(
                this,
                ColorStateList.valueOf(it.second)
            )
        }
    }
}

@BindingAdapter("input_layout_style")
fun TextInputLayout.setInputLayoutStyle(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.FIELD_LABEL_TEXT]?.let { color ->
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_focused),
                    intArrayOf(-android.R.attr.state_focused)
                ),
                intArrayOf(
                    color,
                    color
                )
            )
            defaultHintTextColor = colorStateList
            boxBackgroundColor = color
        }
    }
}

@BindingAdapter("warning", "error")
fun TextView.setWarningOrError(warning: String?, error: String?) {
    if (warning != null) {
        this.text = warning
    } else if (error != null) {
        this.text = error
    }
}

@BindingAdapter("inputWarning", "inputError")
fun TextInputLayout.setWarningErrorMessage(warning: String?, error: String?) {
    when {
        error != null -> {
            setErrorTextAppearance(R.style.error_appearance)
            this.error = error
            editText?.text = null
        }
        warning != null -> {
            setErrorTextAppearance(R.style.warning_appearance)
            this.error = warning
        }
        else -> this.error = null
    }
}

@BindingAdapter("setImeOption")
fun setImeOption(editText: EditText, type: KeyboardActionType?) {
    if (type != null) {
        when (type) {
            KeyboardActionType.NEXT -> editText.imeOptions = IME_ACTION_NEXT
            KeyboardActionType.DONE -> editText.imeOptions = IME_ACTION_DONE
            KeyboardActionType.ENTER -> editText.imeOptions = IME_FLAG_NO_ENTER_ACTION
        }
    }
}

@BindingAdapter("onEditorActionListener")
fun bindOnEditorActionListener(editText: EditText, item: FieldUiModel) {
    editText.setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
            IME_ACTION_NEXT -> {
                item.onNext()
                true
            }
            IME_ACTION_DONE -> {
//                closeKeyboard(context)
                true
            }
            else -> false
        }
    }
}

@BindingAdapter(value = ["onTextChangeListener", "clearButton"], requireAll = false)
fun bindOnTextChangeListener(editText: EditText, item: FieldUiModel, clearButton: ImageView?) {
    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (valueHasChanged(editText.text.toString(), item.value) && editText.hasFocus()) {
                item.onTextChange(charSequence.toString())
            }
            if (item.valueType == ValueType.LONG_TEXT) {
                if (item.editable && editText.text.toString().isNotEmpty()) {
                    clearButton?.visibility = View.VISIBLE
                } else {
                    clearButton?.visibility = View.GONE
                }
            }
        }

        override fun afterTextChanged(editable: Editable) {}
    })
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

private fun valueHasChanged(currentValue: String, storedValue: String?): Boolean {
    return storedValue != currentValue
}

@BindingAdapter("requestFocus")
fun requestFocus(editText: EditText, item: FieldUiModel) {
    if (item.focused) {
        editText.requestFocus()
        editText.isCursorVisible = true
//        editText.openKeyboard()
    } else {
        editText.clearFocus()
        editText.isCursorVisible = false
    }
    editText.setOnTouchListener { _, event ->
        if (event.action == ACTION_UP) {
            item.onItemClick()
        }
        false
    }

    editText.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
            //        editText.openKeyboard()
        } else if (valueHasChanged(editText.text.toString(), item.value)) {
            //sendAction
        }
    }
}