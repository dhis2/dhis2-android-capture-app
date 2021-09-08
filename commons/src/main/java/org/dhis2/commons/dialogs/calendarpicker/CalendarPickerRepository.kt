package org.dhis2.commons.dialogs.calendarpicker

interface CalendarPickerRepository {
    fun isDatePickerStyle(): Boolean
    fun setPickerStyle(isDatePicker: Boolean)
}
