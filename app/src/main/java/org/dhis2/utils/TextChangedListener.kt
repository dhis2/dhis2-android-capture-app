package org.dhis2.utils

import androidx.databinding.adapters.TextViewBindingAdapter
import android.text.Editable

/**
 * QUADRAM. Created by frodriguez on 1/22/2018.
 */

interface TextChangedListener : TextViewBindingAdapter.OnTextChanged {
    fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int)
    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int)
    fun afterTextChanged(editable: Editable)
}
