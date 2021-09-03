package org.dhis2.commons.dialogs.calendarpicker

import android.widget.DatePicker

interface OnDatePickerListener {
    fun onNegativeClick()
    fun onPositiveClick(datePicker: DatePicker)
}
