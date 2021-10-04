package org.dhis2.form.ui.binding

import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.widget.EditText
import androidx.databinding.BindingAdapter
import org.dhis2.form.model.KeyboardActionType

object ItemBindings {

    @JvmStatic
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
}
