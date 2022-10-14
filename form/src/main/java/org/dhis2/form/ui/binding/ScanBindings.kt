package org.dhis2.form.ui.binding

import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.commons.extensions.openKeyboard
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType

@BindingAdapter("action_icon")
fun ImageView.setActionIcon(model: FieldUiModel) {
    val iconRes = when (model.renderingType) {
        UiRenderType.QR_CODE -> R.drawable.ic_form_qr
        UiRenderType.BAR_CODE -> R.drawable.ic_form_barcode
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
fun TextInputEditText.setActionHandler(model: FieldUiModel) {
    isFocusable = model.editable

    setOnTouchListener { v, event ->
        if (MotionEvent.ACTION_UP == event.action) model.onItemClick()
        false
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

    if (model.focused) {
        requestFocus()
        openKeyboard()
    }
}

@BindingAdapter("view_edition")
fun View.setViewEdition(editable: Boolean) {
    alpha = if (editable) 1f else 0.5f
}
