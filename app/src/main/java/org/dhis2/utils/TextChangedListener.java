package org.dhis2.utils;

import android.databinding.adapters.TextViewBindingAdapter;
import android.text.Editable;

/**
 * Created by frodriguez on 1/22/2018.
 */

public interface TextChangedListener extends TextViewBindingAdapter.OnTextChanged {
    void beforeTextChanged(CharSequence charSequence, int start, int count, int after);
    void onTextChanged(CharSequence charSequence, int start, int before, int count);
    void afterTextChanged(Editable editable);
}
