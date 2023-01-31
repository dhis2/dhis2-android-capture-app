package org.dhis2.android.rtsm.utils

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone
import org.dhis2.android.rtsm.commons.Constants

class DateUtils {
    companion object {
        @JvmStatic
        fun getDateTimePattern(): DateTimeFormatter =
            DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT)

        @JvmStatic
        fun getDatePattern(): DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)

        @JvmStatic
        fun getMonthStartToNowConstraint(): CalendarConstraints {
            val constraintsBuilderRange = CalendarConstraints.Builder()

            // Day end (current day)
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val endTime = cal.timeInMillis

            // Day start
            cal.add(Calendar.DAY_OF_YEAR, Constants.MAX_ALLOWABLE_DAYS_BACK_RANGE - 1)
            val startTime = cal.timeInMillis

            val dateValidatorMin = DateValidatorPointForward.from(startTime)
            val dateValidatorMax = DateValidatorPointBackward.before(endTime)
            val validators = CompositeDateValidator.allOf(
                listOf(dateValidatorMin, dateValidatorMax)
            )
            constraintsBuilderRange.setValidator(validators)

            return constraintsBuilderRange.build()
        }
    }
}
