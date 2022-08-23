package org.dhis2.android.rtsm.utils

import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import org.dhis2.android.rtsm.R
import java.time.LocalDateTime

@BindingAdapter("date")
fun setDate(view: AutoCompleteTextView, date: LocalDateTime) =
    view.setText(date.humanReadableDate())

@BindingAdapter("distributedTo")
fun setDistributedTo(view: TextView, s: String?) {
    if (s == null) {
        view.visibility = View.GONE
    } else {
        view.text = s
        view.visibility = View.VISIBLE
    }
}

@BindingAdapter("selected")
fun transactionButtonSelected(button: MaterialButton, selected: Boolean) {
    if (selected) {
        button.setStrokeColorResource(R.color.button_highlight_color)
        button.setStrokeWidthResource(R.dimen.transaction_button_highlight_width)
    } else {
        button.setStrokeColorResource(R.color.transparent)
        button.setStrokeWidthResource(R.dimen.transaction_button_no_highlight_width)
    }
}

@BindingAdapter("totalCount")
fun setCount(view: TextView, count: Int) {
    view.text = view.context.resources.getQuantityString(R.plurals.items_found, count, count)
}