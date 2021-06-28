package org.dhis2.commons.customdialogs

import android.widget.DatePicker

interface OnDatePickerListener {
    fun onNegativeClick()
    fun onPositiveClick(datePicker: DatePicker)
}