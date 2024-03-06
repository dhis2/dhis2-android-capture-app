package org.dhis2.form.ui.event

import java.util.Calendar
import java.util.Date
import org.dhis2.commons.date.DateUtils
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType

class DialogDelegate {

    fun handleDateInput(uid: String, year: Int, month: Int, day: Int): FormIntent {
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
        val date = DateUtils.oldUiDateFormat().format(ageDate)

        return FormIntent.OnSave(
            uid = uid,
            value = date,
            valueType = ValueType.DATE
        )
    }

    fun handleYearMonthDayInput(uid: String, year: Int, month: Int, day: Int): FormIntent {
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
        val date = DateUtils.oldUiDateFormat().format(ageDate)

        return FormIntent.OnSave(
            uid = uid,
            value = date,
            valueType = ValueType.DATE
        )
    }

    fun handleTimeInput(uid: String, date: Date?, hourOfDay: Int, minutes: Int): FormIntent {
        val currentCalendar = Calendar.getInstance()
        val dateTime = with(currentCalendar) {
            date?.let { time = it }
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            return@with time
        }
        val dateValue = when (date) {
            null -> DateUtils.timeFormat().format(dateTime)
            else -> DateUtils.databaseDateFormatNoSeconds().format(dateTime)
        }
        return FormIntent.OnSave(
            uid = uid,
            value = dateValue,
            valueType = date?.let { ValueType.DATETIME } ?: ValueType.TIME
        )
    }

    fun handleDateTimeInput(
        uid: String,
        label: String,
        date: Date?,
        year: Int,
        month: Int,
        day: Int
    ): RecyclerViewUiEvents {
        val currentCalendar = Calendar.getInstance()
        val dateTime = with(currentCalendar) {
            date?.let { time = it }
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            return@with time
        }
        return RecyclerViewUiEvents.OpenTimePicker(uid, label, dateTime, isDateTime = true)
    }
}
