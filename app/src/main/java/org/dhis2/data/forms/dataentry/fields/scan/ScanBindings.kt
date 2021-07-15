package org.dhis2.data.forms.dataentry.fields.scan

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.Bindings.closeKeyboard
import org.dhis2.R
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

@BindingAdapter("action_icon")
fun ImageView.setActionIcon(model: ScanTextViewModel) {
    isClickable = model.editable
    val iconRes = when (model.fieldRendering?.type()) {
        ValueTypeRenderingType.QR_CODE -> R.drawable.ic_form_qr
        ValueTypeRenderingType.BAR_CODE -> R.drawable.ic_form_barcode
        else -> null
    }

    iconRes?.let { setImageResource(iconRes) }
}

@BindingAdapter(value = ["error_message", "warning_message"], requireAll = false)
fun TextInputLayout.setErrorMessage(errorMessage: String?, warningMessage: String?) {
    error = when {
        errorMessage != null -> {
            setErrorTextAppearance(R.style.error_appearance)
            errorMessage
        }
        warningMessage != null -> {
            setErrorTextAppearance(R.style.warning_appearance)
            warningMessage
        }
        else -> null
    }
}

@BindingAdapter("action_handler")
fun TextInputEditText.setActionHandler(model: ScanTextViewModel) {
    isEnabled = model.editable
    isFocusable = true
    isClickable = model.editable

    setOnFocusChangeListener { view, hasFocus ->
        if (hasFocus) {
            model.onItemClick()
        } else {
            model.onScanSelected(text.toString())
        }
    }
    setOnEditorActionListener { v, actionId, _ ->
        when (actionId) {
            EditorInfo.IME_ACTION_NEXT -> {
                model.onNext()
                return@setOnEditorActionListener true
            }
            EditorInfo.IME_ACTION_DONE -> {
                v.closeKeyboard()
                return@setOnEditorActionListener true
            }
            else -> return@setOnEditorActionListener false
        }
    }
    doOnTextChanged { text, _, _, _ -> model.onTextChange(text.toString()) }
}

@BindingAdapter("view_edition")
fun View.setViewEdition(editable: Boolean) {
    alpha = if (editable) 1f else 0.5f
}