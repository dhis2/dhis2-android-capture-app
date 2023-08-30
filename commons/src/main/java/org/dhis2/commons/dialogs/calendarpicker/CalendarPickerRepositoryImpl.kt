package org.dhis2.commons.dialogs.calendarpicker

import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider

class CalendarPickerRepositoryImpl(
    private val preferences: PreferenceProvider,
) : CalendarPickerRepository {

    override fun isDatePickerStyle(): Boolean {
        return if (preferences.contains(Preference.DATE_PICKER)) {
            preferences.getBoolean(Preference.DATE_PICKER, true)
        } else {
            preferences.setValue(Preference.DATE_PICKER, false)
            preferences.getBoolean(Preference.DATE_PICKER, false)
        }
    }

    override fun setPickerStyle(isDatePicker: Boolean) {
        preferences.setValue(Preference.DATE_PICKER, !isDatePicker)
    }
}
