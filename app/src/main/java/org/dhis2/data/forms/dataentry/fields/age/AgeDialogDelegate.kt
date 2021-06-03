package org.dhis2.data.forms.dataentry.fields.age

import java.util.Calendar
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.utils.DateUtils

class AgeDialogDelegate {

    fun handleDateInput(
        uid: String,
        year: Int,
        month: Int,
        day: Int
    ): FormIntent {
        val currentCalendar = Calendar.getInstance()
        val ageDate = with(currentCalendar) {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            return@with time
        }
        val date = if (ageDate == null) null else DateUtils.oldUiDateFormat()
            .format(ageDate)

        return FormIntent.SelectDateFromAgeCalendar(uid, date)
    }

    fun handleYearMonthDayInput(
        uid: String,
        year: Int,
        month: Int,
        day: Int
    ): FormIntent {
        val currentCalendar = Calendar.getInstance()
        val ageDate = with(currentCalendar) {
            add(Calendar.YEAR, year)
            add(Calendar.MONTH, month)
            add(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            return@with time
        }
        val date = if (ageDate == null) null else DateUtils.oldUiDateFormat()
            .format(ageDate)

        return FormIntent.SelectDateFromAgeCalendar(uid, date)
    }
}
