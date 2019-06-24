package org.dhis2.utils

import org.dhis2.utils.DateUtils
import org.dhis2.utils.Period
import org.hisp.dhis.android.core.event.EventModel
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.period.PeriodType
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateUtils {

    companion object{

        const val DATABASE_FORMAT_EXPRESSION = "yyyy-MM-dd'T'HH:mm:ss.SSS"
        const val DATABASE_FORMAT_EXPRESSION_NO_MILLIS = "yyyy-MM-dd'T'HH:mm:ss"
        const val DATABASE_FORMAT_EXPRESSION_NO_SECONDS = "yyyy-MM-dd'T'HH:mm"
        const val DATE_TIME_FORMAT_EXPRESSION = "yyyy-MM-dd HH:mm"
        const val DATE_FORMAT_EXPRESSION = "yyyy-MM-dd"

        private var instance: DateUtils? = null
        lateinit var currentDateCalendar: Calendar

        public fun getInstance(): DateUtils {
            if (instance == null)
                instance = org.dhis2.utils.DateUtils()

            return instance!!
        }

        fun getDifference(startDate: Date?, endDate: Date?): IntArray {
            val interval = org.joda.time.Period(startDate?.time ?: Date().time, endDate?.time ?: Date().time, org.joda.time.PeriodType.yearMonthDayTime())
            return intArrayOf(interval.years, interval.months, interval.days)
        }

        public fun uiDateFormat(): SimpleDateFormat {
            return SimpleDateFormat(DATE_FORMAT_EXPRESSION, Locale.US)
        }

        public fun timeFormat(): SimpleDateFormat {
            return SimpleDateFormat("HH:mm", Locale.US)
        }

        public fun dateTimeFormat(): SimpleDateFormat {
            return SimpleDateFormat(DATE_TIME_FORMAT_EXPRESSION, Locale.US)
        }


        public fun databaseDateFormat(): SimpleDateFormat {
            return SimpleDateFormat(DATABASE_FORMAT_EXPRESSION, Locale.US)
        }


        public fun databaseDateFormatNoMillis(): SimpleDateFormat {
            return SimpleDateFormat(DATABASE_FORMAT_EXPRESSION_NO_MILLIS, Locale.US)
        }

        public fun databaseDateFormatNoSeconds(): SimpleDateFormat {
            return SimpleDateFormat(DATABASE_FORMAT_EXPRESSION_NO_SECONDS, Locale.US)
        }

        public fun dateHasNoSeconds(dateTime: String): Boolean {
            try {
                databaseDateFormatNoSeconds().parse(dateTime)
                return true
            } catch (e: ParseException) {
                return false
            }

        }


        /**********************
         * FORMAT REGION */

    }

    fun getDateFromDateAndPeriod(date: Date, period: Period): Array<Date> {
        return when (period) {
            Period.YEARLY -> arrayOf(getFirstDayOfYear(date), getLastDayOfYear(date))
            Period.MONTHLY -> arrayOf(getFirstDayOfMonth(date), getLastDayOfMonth(date))
            Period.WEEKLY -> arrayOf(getFirstDayOfWeek(date), getLastDayOfWeek(date))
            Period.DAILY -> arrayOf(getDate(date), getNextDate(date))
            else -> arrayOf(getDate(date), getNextDate(date))
        }
    }

    /**********************
     * CURRENT PEDIOD REGION */

    fun getToday(): Date {
        return getCalendar().time
    }

    /**********************
     * SELECTED PEDIOD REGION */

    private fun getDate(date: Date): Date {
        val calendar = getCalendar()

        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    private fun getNextDate(date: Date): Date {
        val calendar = getCalendar()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }


    private fun getFirstDayOfWeek(date: Date): Date {
        val calendar = getCalendar()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        return calendar.time
    }

    private fun getLastDayOfWeek(date: Date): Date {
        val calendar = getCalendar()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.WEEK_OF_YEAR, 1) //Move to next week
        calendar.add(Calendar.DAY_OF_MONTH, -1)//Substract one day to get last day of current week

        return calendar.time
    }

    private fun getFirstDayOfMonth(date: Date): Date {
        val calendar = getCalendar()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.time
    }

    private fun getLastDayOfMonth(date: Date): Date {
        val calendar = getCalendar()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return calendar.time
    }

    private fun getFirstDayOfYear(date: Date): Date {
        val calendar = getCalendar()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.set(Calendar.DAY_OF_YEAR, 1)
        return calendar.time
    }

    private fun getLastDayOfYear(date: Date): Date {
        val calendar = getCalendar()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.add(Calendar.YEAR, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return calendar.time
    }


    fun formatDate(dateToFormat: Date): String {
        return uiDateFormat().format(dateToFormat)
    }

    fun getCalendar(): Calendar {
        if (currentDateCalendar != null)
            return currentDateCalendar

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    fun setCurrentDate(date: Date) {
        currentDateCalendar = getCalendar()
        currentDateCalendar!!.time = date
        currentDateCalendar!!.set(Calendar.HOUR_OF_DAY, 0)
        currentDateCalendar!!.set(Calendar.MINUTE, 0)
        currentDateCalendar!!.set(Calendar.SECOND, 0)
        currentDateCalendar!!.set(Calendar.MILLISECOND, 0)
    }

    /**********************
     * COMPARE DATES REGION */
    @Deprecated("")
    fun hasExpired(event: EventModel, expiryDays: Int, completeEventExpiryDays: Int, expiryPeriodType: PeriodType?): Boolean {
        val expiredDate = Calendar.getInstance()

        if (event.status() == EventStatus.COMPLETED && completeEventExpiryDays == 0) {
            return false
        }

        if (event.completedDate() != null) {
            expiredDate.time = event.completedDate()
        } else {
            expiredDate.time = if (event.eventDate() != null) event.eventDate() else event.dueDate()
            expiredDate.set(Calendar.HOUR_OF_DAY, 23)
        }

        if (expiryPeriodType == null) {
            if (completeEventExpiryDays > 0) {
                expiredDate.add(Calendar.DAY_OF_YEAR, completeEventExpiryDays)
            }
            return expiredDate.time.before(getNextPeriod(expiryPeriodType, expiredDate.time, 0))
        } else {
            when (expiryPeriodType) {
                PeriodType.Daily -> {
                }
                PeriodType.Weekly -> {
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1)
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.firstDayOfWeek)
                }
                PeriodType.WeeklyWednesday -> {
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1)
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.firstDayOfWeek)
                    expiredDate.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                }
                PeriodType.WeeklyThursday -> {
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1)
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.firstDayOfWeek)
                    expiredDate.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                }
                PeriodType.WeeklySaturday -> {
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1)
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.firstDayOfWeek)
                    expiredDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                }
                PeriodType.WeeklySunday -> {
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1)
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.firstDayOfWeek)
                    expiredDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                }
                PeriodType.BiWeekly -> {
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 2)
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.firstDayOfWeek)
                }
                PeriodType.Monthly -> {
                    expiredDate.add(Calendar.MONTH, 1)
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1)
                }
                PeriodType.BiMonthly -> {
                    expiredDate.add(Calendar.MONTH, 2)
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1)
                }
                PeriodType.Quarterly -> {
                    expiredDate.add(Calendar.MONTH, 3)
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1)
                }
                PeriodType.SixMonthly -> {
                    expiredDate.add(Calendar.MONTH, 6)
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1)
                }
                PeriodType.SixMonthlyApril -> {
                    expiredDate.add(Calendar.MONTH, 6)
                    expiredDate.set(Calendar.MONTH, Calendar.APRIL)
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1)
                }
                PeriodType.Yearly -> {
                    expiredDate.add(Calendar.YEAR, 1)
                    expiredDate.set(Calendar.DAY_OF_YEAR, 1)
                }
                PeriodType.FinancialApril -> {
                    expiredDate.add(Calendar.YEAR, 1)
                    expiredDate.set(Calendar.MONTH, Calendar.APRIL)
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1)
                }
                PeriodType.FinancialJuly -> {
                    expiredDate.add(Calendar.YEAR, 1)
                    expiredDate.set(Calendar.MONTH, Calendar.JULY)
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1)
                }
                PeriodType.FinancialOct -> {
                    expiredDate.add(Calendar.YEAR, 1)
                    expiredDate.add(Calendar.MONTH, Calendar.OCTOBER)
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1)
                }
            }
            if (expiryDays > 0)
                expiredDate.add(Calendar.DAY_OF_YEAR, expiryDays)
            return expiredDate.time.before(getToday())
        }

    }



    fun getNewDate(events: List<EventModel>, periodType: PeriodType): Date {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)

        val eventDates = ArrayList<Date>()
        var newDate = Date()
        var needNewDate = true

        for (event in events) {
            eventDates.add(event.eventDate()!!)
        }

        while (needNewDate) {
            when (periodType) {
                PeriodType.Weekly -> {
                    now.time = moveWeekly(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1) //jump one week
                }
                PeriodType.WeeklyWednesday -> {
                    now.time = moveWeeklyWednesday(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1)
                }
                PeriodType.WeeklyThursday -> {
                    now.time = moveWeeklyThursday(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1)
                }
                PeriodType.WeeklySaturday -> {
                    now.time = moveWeeklySaturday(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1)
                }
                PeriodType.WeeklySunday -> {
                    now.time = moveWeeklySunday(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1)
                }
                PeriodType.BiWeekly -> {
                    now.time = moveBiWeekly(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 2)
                }
                PeriodType.Monthly -> {
                    now.time = moveMonthly(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.MONTH, 1)
                }
                PeriodType.BiMonthly -> {
                    now.time = moveBiMonthly(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.MONTH, 2)
                }
                PeriodType.Quarterly -> {
                    now.time = moveQuarterly(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.MONTH, 4)
                }
                PeriodType.SixMonthly -> {
                    now.time = moveSixMonthly(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.MONTH, 6)
                }
                PeriodType.SixMonthlyApril -> {
                    now.time = moveSixMonthlyApril(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.MONTH, 6)
                }
                PeriodType.Yearly -> {
                    now.time = moveYearly(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1)
                }
                PeriodType.FinancialApril -> {
                    now.time = moveFinancialApril(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1)
                }
                PeriodType.FinancialJuly -> {
                    now.time = moveFinancialJuly(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1)
                }
                PeriodType.FinancialOct -> {
                    now.time = moveFinancialOct(now)
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1)
                }
                PeriodType.Daily -> {
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1) //jump one day
                }
                else -> {
                    if (!eventDates.contains(now.time)) {
                        newDate = now.time
                        needNewDate = false
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            now.time = getNextPeriod(periodType, now.time, 1)
        }

        return newDate
    }

    fun moveWeeklyWednesday(date: Calendar): Date {
        if (date.get(Calendar.DAY_OF_WEEK) > 1 && date.get(Calendar.DAY_OF_WEEK) < Calendar.WEDNESDAY)
            date.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        else {
            date.add(Calendar.WEEK_OF_YEAR, 1)
            date.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        }
        return date.time
    }

    fun moveWeeklyThursday(date: Calendar): Date {
        if (date.get(Calendar.DAY_OF_WEEK) > 1 && date.get(Calendar.DAY_OF_WEEK) < Calendar.THURSDAY)
            date.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        else {
            date.add(Calendar.WEEK_OF_YEAR, 1)
            date.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        }
        return date.time
    }

    fun moveWeeklySaturday(date: Calendar): Date {
        if (date.get(Calendar.DAY_OF_WEEK) > 1 && date.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY)
            date.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        else {
            date.add(Calendar.WEEK_OF_YEAR, 1)
            date.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        }
        return date.time
    }

    fun moveWeeklySunday(date: Calendar): Date {
        if (date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            date.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        else {
            date.add(Calendar.WEEK_OF_YEAR, 1)
            date.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        return date.time
    }

    fun moveBiWeekly(date: Calendar): Date {
        date.add(Calendar.WEEK_OF_YEAR, 2)
        date.set(Calendar.DAY_OF_WEEK, date.firstDayOfWeek)
        return date.time
    }

    fun moveWeekly(date: Calendar): Date {
        date.add(Calendar.WEEK_OF_YEAR, 1) //Set to next week
        date.set(Calendar.DAY_OF_WEEK, date.firstDayOfWeek) //Set to first day of next week
        date.add(Calendar.DAY_OF_YEAR, -1) //Set to last day of this week
        return date.time
    }

    fun moveMonthly(date: Calendar): Date {
        date.add(Calendar.MONTH, 1)
        date.set(Calendar.DAY_OF_MONTH, 1)
        date.add(Calendar.DAY_OF_MONTH, -1)
        return date.time
    }

    fun moveBiMonthly(date: Calendar): Date {
        date.add(Calendar.MONTH, 2)
        date.set(Calendar.DAY_OF_MONTH, 1)
        date.add(Calendar.DAY_OF_MONTH, -1)
        return date.time
    }

    fun moveQuarterly(date: Calendar): Date {
        date.add(Calendar.MONTH, 4)
        date.set(Calendar.DAY_OF_MONTH, 1)
        date.add(Calendar.DAY_OF_MONTH, -1)
        return date.time
    }

    fun moveSixMonthly(date: Calendar): Date {
        date.add(Calendar.MONTH, 6)
        date.set(Calendar.DAY_OF_MONTH, 1)
        date.add(Calendar.DAY_OF_MONTH, -1)
        return date.time
    }

    fun moveSixMonthlyApril(date: Calendar): Date {
        if (date.get(Calendar.MONTH) > Calendar.SEPTEMBER && date.get(Calendar.MONTH) <= Calendar.DECEMBER) {
            date.add(Calendar.MONTH, 6)
            date.set(Calendar.MONTH, Calendar.APRIL)
            date.set(Calendar.DAY_OF_MONTH, 1)
        } else if (date.get(Calendar.MONTH) > Calendar.APRIL && date.get(Calendar.MONTH) < Calendar.SEPTEMBER) {
            date.set(Calendar.MONTH, Calendar.SEPTEMBER)
            date.set(Calendar.DAY_OF_MONTH, 1)
        } else {
            date.set(Calendar.DAY_OF_MONTH, Calendar.APRIL)
            date.set(Calendar.DAY_OF_MONTH, 1)
        }
        return date.time
    }

    fun moveYearly(date: Calendar): Date {
        date.add(Calendar.YEAR, 1)
        date.set(Calendar.DAY_OF_YEAR, 1)
        return date.time
    }

    fun moveFinancialApril(date: Calendar): Date {
        date.add(Calendar.YEAR, 1)
        date.set(Calendar.MONTH, Calendar.APRIL)
        date.set(Calendar.DAY_OF_MONTH, 1)
        return date.time
    }

    fun moveFinancialJuly(date: Calendar): Date {
        date.add(Calendar.YEAR, 1)
        date.set(Calendar.MONTH, Calendar.JULY)
        date.set(Calendar.DAY_OF_MONTH, 1)
        return date.time
    }

    fun moveFinancialOct(date: Calendar): Date {
        date.add(Calendar.YEAR, 1)
        date.set(Calendar.MONTH, Calendar.OCTOBER)
        date.set(Calendar.DAY_OF_MONTH, 1)
        return date.time
    }


    /**
     * @param currentDate      Date from which calculation will be carried out. Default value is today.
     * @param expiryDays       Number of extra days to add events on previous period
     * @param expiryPeriodType Expiry Period
     * @return Min date to select
     */
    fun expDate(currentDate: Date?, expiryDays: Int, expiryPeriodType: PeriodType?): Date? {

        val calendar = getCalendar()

        if (currentDate != null)
            calendar.time = currentDate

        val date = calendar.time

        if (expiryPeriodType == null) {
            return null
        } else {
            when (expiryPeriodType) {
                PeriodType.Daily -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -expiryDays)
                    return calendar.time
                }
                PeriodType.Weekly -> {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    val firstDateOfWeek = calendar.time
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfWeek.time) >= expiryDays) {
                        return firstDateOfWeek
                    } else {
                        calendar.add(Calendar.WEEK_OF_YEAR, -1)
                    }
                }
                PeriodType.WeeklyWednesday -> {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY) //moves to current week wednesday
                    val wednesday = calendar.time
                    if (TimeUnit.MILLISECONDS.toDays(date.time - wednesday.time) >= expiryDays)
                        return wednesday
                    else
                        calendar.add(Calendar.WEEK_OF_YEAR, -1)
                }
                PeriodType.WeeklyThursday -> {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY) //moves to current week wednesday
                    val thursday = calendar.time
                    if (TimeUnit.MILLISECONDS.toDays(date.time - thursday.time) >= expiryDays)
                        return thursday
                    else
                        calendar.add(Calendar.WEEK_OF_YEAR, -1)
                }
                PeriodType.WeeklySaturday -> {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) //moves to current week wednesday
                    val saturday = calendar.time
                    if (TimeUnit.MILLISECONDS.toDays(date.time - saturday.time) >= expiryDays)
                        return saturday
                    else
                        calendar.add(Calendar.WEEK_OF_YEAR, -1)
                }
                PeriodType.WeeklySunday -> {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) //moves to current week wednesday
                    val sunday = calendar.time
                    if (TimeUnit.MILLISECONDS.toDays(date.time - sunday.time) >= expiryDays)
                        return sunday
                    else
                        calendar.add(Calendar.WEEK_OF_YEAR, -1)
                }
                PeriodType.BiWeekly -> {
                    if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0)
                    //if true, we are in the 2nd week of the period
                        calendar.add(Calendar.WEEK_OF_YEAR, -1)//Moved to first week
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    val firstDateOfBiWeek = calendar.time
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfBiWeek.time) >= expiryDays) {
                        return firstDateOfBiWeek
                    } else {
                        calendar.add(Calendar.WEEK_OF_YEAR, -2)
                    }
                }
                PeriodType.Monthly -> {
                    val firstDateOfMonth = getFirstDayOfMonth(calendar.time)
                    calendar.time = firstDateOfMonth
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfMonth.time) >= expiryDays) {
                        return firstDateOfMonth
                    } else {
                        calendar.add(Calendar.MONTH, -1)
                    }
                }
                PeriodType.BiMonthly -> {
                    if (calendar.get(Calendar.MONTH) % 2 != 0)
                    //January is 0, December is 11
                        calendar.add(Calendar.MONTH, -1) //Moved to first month
                    val firstDateOfBiMonth = getFirstDayOfMonth(calendar.time)
                    calendar.time = firstDateOfBiMonth
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfBiMonth.time) >= expiryDays) {
                        return firstDateOfBiMonth
                    } else {
                        calendar.add(Calendar.MONTH, -2)
                    }
                }
                PeriodType.Quarterly -> {
                    while (calendar.get(Calendar.MONTH) % 4 != 0)
                    //January is 0, December is 11
                        calendar.add(Calendar.MONTH, -1) //Moved to first month
                    val firstDateOfQMonth = getFirstDayOfMonth(calendar.time)
                    calendar.time = firstDateOfQMonth
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfQMonth.time) >= expiryDays) {
                        return firstDateOfQMonth
                    } else {
                        calendar.add(Calendar.MONTH, -4)
                    }
                }
                PeriodType.SixMonthly -> {
                    while (calendar.get(Calendar.MONTH) % 6 != 0)
                    //January is 0, December is 11
                        calendar.add(Calendar.MONTH, -1) //Moved to first month
                    val firstDateOfSixMonth = getFirstDayOfMonth(calendar.time)
                    calendar.time = firstDateOfSixMonth
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfSixMonth.time) >= expiryDays) {
                        return firstDateOfSixMonth
                    } else {
                        calendar.add(Calendar.MONTH, -6)
                    }
                }
                PeriodType.SixMonthlyApril -> {
                    while ((calendar.get(Calendar.MONTH) - 3) % 6 != 0)
                    //April is 0, December is 8
                        calendar.add(Calendar.MONTH, -1) //Moved to first month
                    val firstDateOfSixMonthApril = getFirstDayOfMonth(calendar.time)
                    calendar.time = firstDateOfSixMonthApril
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfSixMonthApril.time) >= expiryDays) {
                        return firstDateOfSixMonthApril
                    } else {
                        calendar.add(Calendar.MONTH, -6)
                    }
                }
                PeriodType.Yearly -> {
                    val firstDateOfYear = getFirstDayOfYear(calendar.time)
                    calendar.time = firstDateOfYear
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfYear.time) >= expiryDays) {
                        return firstDateOfYear
                    } else {
                        calendar.add(Calendar.YEAR, -1)
                    }
                }
                PeriodType.FinancialApril -> {
                    calendar.set(Calendar.MONTH, Calendar.APRIL)//Moved to April
                    val firstDateOfAprilYear = getFirstDayOfMonth(calendar.time) //first day of April
                    calendar.time = firstDateOfAprilYear
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfAprilYear.time) >= expiryDays) {
                        return firstDateOfAprilYear
                    } else {
                        calendar.add(Calendar.YEAR, -1) //Moved to April last year
                    }
                }
                PeriodType.FinancialJuly -> {
                    calendar.set(Calendar.MONTH, Calendar.JULY)//Moved to July
                    val firstDateOfJulyYear = getFirstDayOfMonth(calendar.time) //first day of July
                    calendar.time = firstDateOfJulyYear
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfJulyYear.time) >= expiryDays) {
                        return firstDateOfJulyYear
                    } else {
                        calendar.add(Calendar.YEAR, -1) //Moved to July last year
                    }
                }
                PeriodType.FinancialOct -> {
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER)//Moved to October
                    val firstDateOfOctYear = getFirstDayOfMonth(calendar.time) //first day of October
                    calendar.time = firstDateOfOctYear
                    if (TimeUnit.MILLISECONDS.toDays(date.time - firstDateOfOctYear.time) >= expiryDays) {
                        return firstDateOfOctYear
                    } else {
                        calendar.add(Calendar.YEAR, -1) //Moved to October last year
                    }
                }
            }

            return calendar.time
        }
    }

    /**
     * @param period      Period in which the date will be selected
     * @param currentDate Current selected date
     * @param page        1 for next, 0 for now, -1 for previous
     * @return Next/Previous date calculated from the currentDate and Period
     */
    fun getNextPeriod(period: PeriodType?, currentDate: Date, page: Int): Date {
        var period = period

        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        val extra: Int
        if (period == null)
            period = PeriodType.Daily

        when (period) {
            PeriodType.Daily -> calendar.add(Calendar.DAY_OF_YEAR, page)
            PeriodType.Weekly -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }
            PeriodType.WeeklyWednesday -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
            }
            PeriodType.WeeklyThursday -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
            }
            PeriodType.WeeklySaturday -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            }
            PeriodType.WeeklySunday -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            }
            PeriodType.BiWeekly -> {
                extra = if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) 1 else 2
                calendar.add(Calendar.WEEK_OF_YEAR, page * extra)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }
            PeriodType.Monthly -> {
                calendar.add(Calendar.MONTH, page)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.BiMonthly -> {
                extra = if ((calendar.get(Calendar.MONTH) + 1) % 2 == 0) 1 else 2
                calendar.add(Calendar.MONTH, page * extra)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.Quarterly -> {
                extra = 1 + 4 - (calendar.get(Calendar.MONTH) + 1) % 4
                calendar.add(Calendar.MONTH, page * extra)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.SixMonthly -> {
                extra = 1 + 6 - (calendar.get(Calendar.MONTH) + 1) % 6
                calendar.add(Calendar.MONTH, page * extra)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.SixMonthlyApril -> {
                if (calendar.get(Calendar.MONTH) < Calendar.APRIL) {
                    calendar.add(Calendar.YEAR, -1)
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER)
                } else if (calendar.get(Calendar.MONTH) >= Calendar.APRIL && calendar.get(Calendar.MONTH) < Calendar.OCTOBER)
                    calendar.set(Calendar.MONTH, Calendar.APRIL)
                else
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, page * 6)
            }
            PeriodType.Yearly -> {
                calendar.add(Calendar.YEAR, page)
                calendar.set(Calendar.DAY_OF_YEAR, 1)
            }
            PeriodType.FinancialApril -> {
                if (calendar.get(Calendar.MONTH) < Calendar.APRIL) {
                    calendar.add(Calendar.YEAR, -1)
                    calendar.set(Calendar.MONTH, Calendar.APRIL)
                } else
                    calendar.set(Calendar.MONTH, Calendar.APRIL)

                calendar.add(Calendar.YEAR, page)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.FinancialJuly -> {
                if (calendar.get(Calendar.MONTH) < Calendar.JULY) {
                    calendar.add(Calendar.YEAR, -1)
                    calendar.set(Calendar.MONTH, Calendar.JULY)
                } else
                    calendar.set(Calendar.MONTH, Calendar.JULY)
                calendar.add(Calendar.YEAR, page)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.FinancialOct -> {
                if (calendar.get(Calendar.MONTH) < Calendar.OCTOBER) {
                    calendar.add(Calendar.YEAR, -1)
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER)
                } else
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER)

                calendar.add(Calendar.YEAR, page)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            else -> {
            }
        }
        return calendar.time
    }

    /**
     * @param period      Period in which the date will be selected
     * @param currentDate Current selected date
     * @param page        1 for next, 0 for now, -1 for previous
     * @return Next/Previous date calculated from the currentDate and Period
     */
    fun getNextPeriod(period: PeriodType?, currentDate: Date, page: Int, lastDate: Boolean): Date {
        var period = period

        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        val extra: Int
        if (period == null)
            period = PeriodType.Daily

        when (period) {
            PeriodType.Daily -> calendar.add(Calendar.DAY_OF_YEAR, page)
            PeriodType.Weekly -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            }
            PeriodType.WeeklyWednesday -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
            }
            PeriodType.WeeklyThursday -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }
            PeriodType.WeeklySaturday -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            }
            PeriodType.WeeklySunday -> {
                calendar.add(Calendar.WEEK_OF_YEAR, page)
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            }
            PeriodType.BiWeekly -> {
                extra = if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) 1 else 2
                calendar.add(Calendar.WEEK_OF_YEAR, page * extra)
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            }
            PeriodType.Monthly -> {
                calendar.add(Calendar.MONTH, page)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.BiMonthly -> {
                extra = if ((calendar.get(Calendar.MONTH) + 1) % 2 == 0) 1 else 2
                calendar.add(Calendar.MONTH, page * extra)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.Quarterly -> {
                extra = 1 + 4 - (calendar.get(Calendar.MONTH) + 1) % 4
                calendar.add(Calendar.MONTH, page * extra)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.SixMonthly -> {
                extra = 1 + 6 - (calendar.get(Calendar.MONTH) + 1) % 6
                calendar.add(Calendar.MONTH, page * extra)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.SixMonthlyApril -> {
                if (calendar.get(Calendar.MONTH) < Calendar.APRIL) {
                    calendar.add(Calendar.YEAR, -1)
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER)
                } else if (calendar.get(Calendar.MONTH) >= Calendar.APRIL && calendar.get(Calendar.MONTH) < Calendar.OCTOBER)
                    calendar.set(Calendar.MONTH, Calendar.APRIL)
                else
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, page * 6)
            }
            PeriodType.Yearly -> {
                calendar.add(Calendar.YEAR, page)
                calendar.set(Calendar.DAY_OF_YEAR, 1)
            }
            PeriodType.FinancialApril -> {
                if (calendar.get(Calendar.MONTH) < Calendar.APRIL) {
                    calendar.add(Calendar.YEAR, -1)
                    calendar.set(Calendar.MONTH, Calendar.APRIL)
                } else
                    calendar.set(Calendar.MONTH, Calendar.APRIL)

                calendar.add(Calendar.YEAR, page)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.FinancialJuly -> {
                if (calendar.get(Calendar.MONTH) < Calendar.JULY) {
                    calendar.add(Calendar.YEAR, -1)
                    calendar.set(Calendar.MONTH, Calendar.JULY)
                } else
                    calendar.set(Calendar.MONTH, Calendar.JULY)
                calendar.add(Calendar.YEAR, page)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodType.FinancialOct -> {
                if (calendar.get(Calendar.MONTH) < Calendar.OCTOBER) {
                    calendar.add(Calendar.YEAR, -1)
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER)
                } else
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER)

                calendar.add(Calendar.YEAR, page)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            else -> {
            }
        }
        return calendar.time
    }

    fun getPeriodUIString(periodType: PeriodType?, date: Date, locale: Locale): String {
        var periodType = periodType

        val formattedDate: String
        val initDate = getNextPeriod(periodType, date, 0)

        val cal = getCalendar()
        cal.time = getNextPeriod(periodType, date, 1)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val endDate = cal.time
        val periodString = "%s - %s"
        if (periodType == null)
            periodType = PeriodType.Daily
        when (periodType) {
            PeriodType.Weekly -> formattedDate = SimpleDateFormat("w yyyy", locale).format(initDate)
            PeriodType.WeeklyWednesday -> formattedDate = SimpleDateFormat("w yyyy", locale).format(initDate)
            PeriodType.WeeklyThursday -> formattedDate = SimpleDateFormat("w yyyy", locale).format(initDate)
            PeriodType.WeeklySaturday -> formattedDate = SimpleDateFormat("w yyyy", locale).format(initDate)
            PeriodType.WeeklySunday -> formattedDate = SimpleDateFormat("w yyyy", locale).format(initDate)
            PeriodType.BiWeekly -> formattedDate = String.format(periodString,
                    SimpleDateFormat("w yyyy", locale).format(initDate),
                    SimpleDateFormat("w yyyy", locale).format(endDate)
            )
            PeriodType.Monthly -> formattedDate = SimpleDateFormat("MMM yyyy", locale).format(initDate)
            PeriodType.BiMonthly -> formattedDate = String.format(periodString,
                    SimpleDateFormat("MMM yyyy", locale).format(initDate),
                    SimpleDateFormat("MMM yyyy", locale).format(endDate)
            )
            PeriodType.Quarterly -> formattedDate = String.format(periodString,
                    SimpleDateFormat("MMM yyyy", locale).format(initDate),
                    SimpleDateFormat("MMM yyyy", locale).format(endDate)
            )
            PeriodType.SixMonthly -> formattedDate = String.format(periodString,
                    SimpleDateFormat("MMM yyyy", locale).format(initDate),
                    SimpleDateFormat("MMM yyyy", locale).format(endDate)
            )
            PeriodType.SixMonthlyApril -> formattedDate = String.format(periodString,
                    SimpleDateFormat("MMM yyyy", locale).format(initDate),
                    SimpleDateFormat("MMM yyyy", locale).format(endDate)
            )
            PeriodType.Yearly -> formattedDate = SimpleDateFormat("yyyy", locale).format(initDate)
            PeriodType.FinancialApril -> formattedDate = String.format(periodString,
                    SimpleDateFormat("MMM yyyy", locale).format(initDate),
                    SimpleDateFormat("MMM yyyy", locale).format(endDate)
            )
            PeriodType.FinancialJuly -> formattedDate = String.format(periodString,
                    SimpleDateFormat("MMM yyyy", locale).format(initDate),
                    SimpleDateFormat("MMM yyyy", locale).format(endDate)
            )
            PeriodType.FinancialOct -> formattedDate = String.format(periodString,
                    SimpleDateFormat("MMM yyyy", locale).format(initDate),
                    SimpleDateFormat("MMM yyyy", locale).format(endDate)
            )
            PeriodType.Daily -> formattedDate = uiDateFormat().format(initDate)
            else -> formattedDate = uiDateFormat().format(initDate)
        }

        return formattedDate
    }

    /**
     * Check if an event is expired in a date
     *
     * @param currentDate  date or today if null
     * @param completedDay date that event was completed
     * @param compExpDays  days of expiration of an event
     * @return true or false
     */
    fun isEventExpired(currentDate: Date?, completedDay: Date?, compExpDays: Int): Boolean {

        val calendar = getCalendar()

        if (currentDate != null)
            calendar.time = currentDate

        val date = calendar.time

        return completedDay != null && compExpDays > 0 &&
                completedDay.time + TimeUnit.DAYS.toMillis(compExpDays.toLong()) < date.time
    }

    /**
     * Check if an event is expired today.
     *
     * @param eventDate         Date of the event (Can be either eventDate or dueDate, but can not be null).
     * @param completeDate      date that event was completed (can be null).
     * @param status            status of event (ACTIVE,COMPLETED,SCHEDULE,OVERDUE,SKIPPED,VISITED).
     * @param compExpDays       extra days to edit event when completed .
     * @param programPeriodType period in which the event can be edited.
     * @param expDays           extra days after period to edit event.
     * @return true or false
     */
    fun isEventExpired(eventDate: Date, completeDate: Date?, status: EventStatus, compExpDays: Int, programPeriodType: PeriodType?, expDays: Int): Boolean? {
        if (status == EventStatus.COMPLETED && completeDate == null)
        //            throw new NullPointerException("completeDate can't be null if status of event is COMPLETED");
            return false

        val expiredBecouseOfPeriod: Boolean
        var expiredBecouseOfCompletion = false

        expiredBecouseOfCompletion = if (status == EventStatus.COMPLETED)
            isEventExpired(null, eventDate, compExpDays)
        else
            false

        return if (programPeriodType != null) {
            var expDate: Date? = getNextPeriod(programPeriodType, eventDate, 1) //Initial date of next period
            if (expDays > 0) {
                val calendar = getCalendar()
                calendar.time = expDate
                calendar.add(Calendar.DAY_OF_YEAR, expDays)
                expDate = calendar.time
            }

            expiredBecouseOfPeriod = expDate != null && expDate.before(getCalendar().time)

            expiredBecouseOfPeriod || expiredBecouseOfCompletion
        } else
            expiredBecouseOfCompletion

    }

    fun getDatePeriodListFor(selectedDates: List<Date>, period: Period): List<DatePeriod> {
        val datePeriods = ArrayList<DatePeriod>()
        for (date in selectedDates) {
            val startEndDates = getDateFromDateAndPeriod(date, period)
            datePeriods.add(DatePeriod.builder().startDate(startEndDates[0]).endDate(startEndDates[1]).build())
        }
        return datePeriods
    }

}