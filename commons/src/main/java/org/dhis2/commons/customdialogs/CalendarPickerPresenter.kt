package org.dhis2.commons.customdialogs

interface CalendarPickerPresenter {
    fun isDatePickerStyle(): Boolean
    fun setPickerStyle(isDatePicker: Boolean)
}