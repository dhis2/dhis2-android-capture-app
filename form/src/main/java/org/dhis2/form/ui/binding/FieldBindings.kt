package org.dhis2.form.ui.binding

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.dhis2.commons.customviews.TextInputAutoCompleteTextView
import org.dhis2.commons.extensions.Preconditions.Companion.equals
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.commons.extensions.openKeyboard
import org.dhis2.commons.prefs.SHARE_PREFS
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.form.R
import org.dhis2.form.databinding.DataElementLegendBinding
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.KeyboardActionType
import org.dhis2.form.model.LegendValue
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
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
        uiModel.backGroundColor?.let { pair ->
            pair.second?.let { color ->
                ViewCompat.setBackgroundTintList(
                    this,
                    ColorStateList.valueOf(color),
                )
            }
        }
    }

    styleItem?.style?.let {
        it.getColors()[FormUiColorType.FIELD_LABEL_TEXT]?.let { color ->
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_focused),
                    intArrayOf(-android.R.attr.state_focused),
                ),
                intArrayOf(
                    color,
                    color,
                ),
            )
            setHintTextColor(colorStateList)
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
                    intArrayOf(-android.R.attr.state_focused),
                ),
                intArrayOf(
                    color,
                    color,
                ),
            )
            defaultHintTextColor = colorStateList
            boxBackgroundColor = color
        }
    }
}

@BindingAdapter("warning", "error")
fun TextView.setWarningOrError(warning: String?, error: String?) {
    if (warning != null) {
        TextViewCompat.setTextAppearance(this, R.style.warning_appearance)
        this.text = warning
    } else if (error != null) {
        TextViewCompat.setTextAppearance(this, R.style.error_appearance)
        this.text = error
    }
}

@BindingAdapter("inputWarning", "inputError")
fun TextInputLayout.setWarningErrorMessage(warning: String?, error: String?) {
    when {
        error != null -> {
            setErrorTextAppearance(R.style.error_appearance)
            this.error = error
        }
        warning != null -> {
            setErrorTextAppearance(R.style.warning_appearance)
            this.error = warning
        }
        else -> this.error = null
    }
}

@BindingAdapter("setOnTouchListener")
fun bindOnTouchListener(editText: EditText, item: FieldUiModel?) {
    editText.setOnTouchListener { _: View?, event: MotionEvent ->
        if (MotionEvent.ACTION_UP == event.action && item?.focused != true) item?.onItemClick()
        false
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

@BindingAdapter("setInputType")
fun EditText.bindInputType(valueType: ValueType) {
    val inputType = when (valueType) {
        ValueType.TEXT -> InputType.TYPE_CLASS_TEXT
        ValueType.LONG_TEXT ->
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
        ValueType.LETTER ->
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        ValueType.NUMBER ->
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL or
                InputType.TYPE_NUMBER_FLAG_SIGNED
        ValueType.UNIT_INTERVAL ->
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        ValueType.PERCENTAGE -> InputType.TYPE_CLASS_NUMBER
        ValueType.INTEGER_NEGATIVE,
        ValueType.INTEGER,
        ->
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_SIGNED
        ValueType.INTEGER_POSITIVE,
        ValueType.INTEGER_ZERO_OR_POSITIVE,
        -> InputType.TYPE_CLASS_NUMBER
        ValueType.PHONE_NUMBER -> InputType.TYPE_CLASS_PHONE
        ValueType.EMAIL ->
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        ValueType.URL -> InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
        else -> null
    }

    inputType?.let { this.inputType = it }
}

@BindingAdapter(value = ["onTextClearListener", "clearButton"], requireAll = false)
fun EditText.bindOnTextClearListener(item: FieldUiModel, clearButton: ImageView?) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            if (item.valueType == ValueType.LONG_TEXT) {
                if (item.editable && editable.toString().isNotEmpty()) {
                    clearButton?.visibility = View.VISIBLE
                } else {
                    clearButton?.visibility = View.GONE
                }
            }
        }
    })
}

private fun valueHasChanged(currentValue: Editable, storedValue: String?): Boolean {
    return !equals(
        if (TextUtils.isEmpty(currentValue)) "" else currentValue.toString(),
        storedValue ?: "",
    )
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
                            intArrayOf(-android.R.attr.state_checked),
                        ),
                        intArrayOf(
                            primaryColor,
                            textPrimaryColor,
                        ),
                    )
                    buttonTintList = colorStateList
                    setTextColor(colorStateList)
                }
            }
        }
    }
}

@BindingAdapter("legendBadge")
fun setLegendBadge(legendLayout: FrameLayout, legendValue: LegendValue?) {
    legendLayout.visibility = if (legendValue != null) View.VISIBLE else View.GONE
    if (legendValue != null) {
        val legendBinding: DataElementLegendBinding = DataElementLegendBinding.inflate(
            LayoutInflater.from(legendLayout.context),
        )
        legendBinding.legend = legendValue
        legendLayout.removeAllViews()
        legendLayout.addView(legendBinding.root)
    }
}

@BindingAdapter("legendValue")
fun TextView.setLegend(legendValue: LegendValue?) {
    legendValue?.let {
        DrawableCompat.setTint(background, ColorUtils().withAlpha(it.color, 38))
        compoundDrawables
            .filterNotNull()
            .forEach { drawable -> DrawableCompat.setTint(drawable, it.color) }
    }
}

@BindingAdapter("drawable_color")
fun TextInputEditText.setDrawableColor(color: Int) {
    compoundDrawablesRelative.filterNotNull().forEach {
        val wrapDrawable: Drawable = DrawableCompat.wrap(it)
        DrawableCompat.setTint(wrapDrawable, color)
    }
}

@BindingAdapter("requestFocus")
fun bindRequestFocus(editText: EditText, focused: Boolean) {
    if (focused) {
        editText.requestFocus()
        editText.isCursorVisible = true
        editText.openKeyboard()
    } else {
        editText.clearFocus()
        editText.isCursorVisible = false
    }
    editText.setSelection(editText.length())
}

@BindingAdapter("setOnEditorActionListener")
fun EditText.bindOnEditorActionListener(item: FieldUiModel) {
    setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
            IME_ACTION_NEXT -> {
                item.onNext()
                true
            }
            IME_ACTION_DONE -> {
                closeKeyboard()
                true
            }
            else -> false
        }
    }
}

@BindingAdapter("setOnFocusChangeListener")
fun EditText.bindOnFocusChangeListener(item: FieldUiModel) {
    setOnFocusChangeListener { _, hasFocus ->
        val value = if (text.isEmpty()) {
            null
        } else {
            text.toString()
        }

        if (!hasFocus && valueHasChanged(text, item.value)) {
            checkAutocompleteRendering(context, item, value)
        }
    }
}

@BindingAdapter("setLongCLickToClipboard")
fun EditText.bindLongClickToClipboard(item: FieldUiModel) {
    setOnLongClickListener {
        item.invokeUiEvent(UiEventType.COPY_TO_CLIPBOARD)
        true
    }
}

@BindingAdapter("setFilters")
fun EditText.bindSetFilters(valueType: ValueType) {
    filters = when (valueType) {
        ValueType.TEXT -> arrayOf<InputFilter>(InputFilter.LengthFilter(50000))
        ValueType.LETTER -> {
            arrayOf(
                InputFilter.LengthFilter(1),
                InputFilter { source: CharSequence, _: Int, _: Int, _: Spanned?, _: Int, _: Int ->
                    when {
                        source.toString().isEmpty() -> {
                            source.toString()
                        }
                        source.toString().matches(Regex("[a-zA-Z]")) -> {
                            source.toString()
                        }
                        else -> {
                            ""
                        }
                    }
                },
            )
        }
        else -> arrayOf()
    }
}

@BindingAdapter("setRenderingType")
fun TextInputAutoCompleteTextView.bindRenderingType(item: FieldUiModel) {
    if (item.renderingType == UiRenderType.AUTOCOMPLETE) {
        val autoCompleteValues = getListFromPreference(context, item.uid)
        val autoCompleteAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_dropdown_item_1line,
            autoCompleteValues,
        )
        setAdapter(autoCompleteAdapter)
    }
}

@BindingAdapter("checkListener")
fun RadioGroup.checkListener(item: FieldUiModel) {
    this.setOnCheckedChangeListener(null)
    when {
        item.isAffirmativeChecked -> this.check(R.id.yes)
        item.isNegativeChecked -> this.check(R.id.no)
        else -> this.clearCheck()
    }
    this.setOnCheckedChangeListener { _, checkedId ->
        when (checkedId) {
            R.id.yes -> item.onSaveBoolean(true)
            R.id.no -> item.onSaveBoolean(false)
        }
    }
}

@BindingAdapter(value = ["onTyping", "textWatcher"], requireAll = true)
fun EditText.setOnTyping(item: FieldUiModel, textWatcher: TextWatcher) {
    removeTextChangedListener(textWatcher)
    setText(item.value)
    setSelection(length())
    if (item.focused) {
        addTextChangedListener(textWatcher)
    }
}

fun getListFromPreference(context: Context, uid: String): MutableList<String> {
    val gson = Gson()
    val json = context.getSharedPreferences(
        SHARE_PREFS,
        Context.MODE_PRIVATE,
    ).getString(uid, "[]")
    val type = object : TypeToken<List<String>>() {}.type
    return gson.fromJson(json, type)
}

fun checkAutocompleteRendering(context: Context, item: FieldUiModel, value: String?) {
    if (item.renderingType == UiRenderType.AUTOCOMPLETE && value != null) {
        val autoCompleteValues = getListFromPreference(context, item.uid)
        if (!autoCompleteValues.contains(value)) {
            autoCompleteValues.add(value)
            saveListToPreference(context, item.uid, autoCompleteValues)
        }
    }
}

fun saveListToPreference(context: Context, uid: String, list: List<String>) {
    val gson = Gson()
    val json = gson.toJson(list)
    context.getSharedPreferences(
        SHARE_PREFS,
        Context.MODE_PRIVATE,
    )
        .edit().putString(uid, json).apply()
}

@BindingAdapter("iconIsClickable")
fun setDescriptionIconVisibility(imageView: View, item: FieldUiModel) {
    imageView.isClickable = false
    if (item.style?.getDescriptionIcon() != null &&
        !item.value.isNullOrEmpty() &&
        item.error.isNullOrEmpty()
    ) {
        imageView.isClickable = true
    }
}
