package org.dhis2.mobile.commons.dates

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.periodUntil
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Returns today's date in the system time zone. Used as the default "now" for age calculations.
 */
@OptIn(ExperimentalTime::class)
fun systemToday(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

/**
 * Calculates a date from an age value and time unit
 * Uses kotlinx.datetime for date arithmetic
 * @param ageValue The age value as an integer
 * @param timeUnit The time unit (years, months, or days) - expects string values: "YEARS", "MONTHS", "DAYS"
 * @param today The reference date to count back from. Defaults to today in the system time zone;
 * pass an explicit value to make the calculation deterministic (tests).
 * @return The calculated date in yyyy-MM-dd format, or null if calculation fails
 */
fun calculateDateFromAge(
    ageValue: String,
    timeUnit: String,
    today: LocalDate = systemToday(),
): String? {
    return try {
        val calculatedDate =
            when (timeUnit) {
                "YEARS" -> today.minus(ageValue.toInt(), DateTimeUnit.YEAR)
                "MONTHS" -> today.minus(ageValue.toInt(), DateTimeUnit.MONTH)
                "DAYS" -> today.minus(ageValue.toInt(), DateTimeUnit.DAY)
                else -> return null
            }

        calculatedDate.toString()
    } catch (_: Exception) {
        null
    }
}

/**
 * Calculates age from a date string based on the specified time unit
 * Uses kotlinx.datetime for period calculations
 * @param dateString The birth date in yyyy-MM-dd format
 * @param timeUnit The time unit to calculate age in - expects string values: "YEARS", "MONTHS", "DAYS"
 * @param today The reference date to measure up to. Defaults to today in the system time zone;
 * pass an explicit value to make the calculation deterministic (tests).
 * @return The calculated age as a string, or null if calculation fails
 */
fun calculateAgeFromDate(
    dateString: String,
    timeUnit: String,
    today: LocalDate = systemToday(),
): String? =
    try {
        val birthDate = LocalDate.parse(dateString)

        when (timeUnit) {
            "YEARS" -> {
                val period = birthDate.periodUntil(today)
                period.years.toString()
            }

            "MONTHS" -> {
                monthsBetween(birthDate, today).toString()
            }

            "DAYS" -> {
                birthDate.daysUntil(today).toString()
            }

            else -> null
        }
    } catch (_: Exception) {
        null
    }

/**
 * Calculates the number of months between two dates
 * Uses kotlinx.datetime for accurate month calculations
 * @param startDate The start date
 * @param endDate The end date
 * @return The number of months between the two dates
 */
fun monthsBetween(
    startDate: LocalDate,
    endDate: LocalDate,
): Int {
    val startDateTotalMonths = 12 * startDate.year + startDate.month.number
    val endDateTotalMonths = 12 * endDate.year + endDate.month.number
    return endDateTotalMonths - startDateTotalMonths
}
