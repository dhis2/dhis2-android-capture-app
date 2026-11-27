package org.dhis2.mobile.commons.extensions

import org.hisp.dhis.mobile.ui.designsystem.component.AgeInputType
import org.hisp.dhis.mobile.ui.designsystem.component.TimeUnitValues
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun String.getDateFromAge(age: AgeInputType.Age): String? {
    val calendar = Calendar.getInstance()
    return try {
        when (age.unit) {
            TimeUnitValues.YEARS -> calendar.add(Calendar.YEAR, -age.value.text.toInt())
            TimeUnitValues.MONTHS -> calendar.add(Calendar.MONTH, -age.value.text.toInt())
            TimeUnitValues.DAYS -> calendar.add(Calendar.DAY_OF_MONTH, -age.value.text.toInt())
        }

        val dateFormat = SimpleDateFormat(DB_FORMAT, Locale.ENGLISH)
        dateFormat.format(calendar.time)
    } catch (e: Exception) {
        null
    }
}

fun String.hasDateFormat(format: String = DB_FORMAT): Boolean =
    try {
        val dateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        dateFormat.parse(this)
        true
    } catch (e: Exception) {
        false
    }

private const val DB_FORMAT = "yyyy-MM-dd"
