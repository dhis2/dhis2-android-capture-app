package org.dhis2.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * QUADRAM. Created by ppajuelo on 16/01/2018.
 */

public class DateUtils {

    public static final int NEXT = 1;
    public static final int PREVIOUS = -1;
    public static final int NOW = 0;

    public static DateUtils getInstance() {
        return new DateUtils();
    }

    public static final String DATABASE_FORMAT_EXPRESSION = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String DATABASE_FORMAT_EXPRESSION_NO_MILLIS = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_TIME_FORMAT_EXPRESSION = "yyyy-MM-dd HH:mm";
    private static final String DATE_FORMAT_EXPRESSION = "yyyy-MM-dd";

    public Date[] getDateFromDateAndPeriod(Date date, Period period) {
        switch (period) {
            case YEARLY:
                return new Date[]{getFirstDayOfYear(date), getLastDayOfYear(date)};
            case MONTHLY:
                return new Date[]{getFirstDayOfMonth(date), getLastDayOfMonth(date)};
            case WEEKLY:
                return new Date[]{getFirstDayOfWeek(date), getLastDayOfWeek(date)};
            case DAILY:
            default:
                return new Date[]{getDate(date), getNextDate(date)};
        }
    }

    /**********************
     CURRENT PEDIOD REGION*/

    public Date getToday() {
        return Calendar.getInstance().getTime();
    }


    private Date getFirstDayOfCurrentWeek() {

        Calendar calendar = getCalendar();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        return calendar.getTime();
    }

    public Date getLastDayOfCurrentWeek() {

        Calendar calendar = getCalendar();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.add(Calendar.WEEK_OF_YEAR, 1); //Move to next week
        calendar.add(Calendar.DAY_OF_MONTH, -1);//Substract one day to get last day of current week

        return calendar.getTime();
    }

    public Date getFirstDayOfurrentMonth() {
        Calendar calendar = getCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public Date getLastDayOfurrentMonth() {
        Calendar calendar = getCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    public Date getFirstDayOfCurrentYear() {
        Calendar calendar = getCalendar();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    public Date getLastDayOfCurrentYear() {
        Calendar calendar = getCalendar();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    /**********************
     SELECTED PEDIOD REGION*/

    public Date getDate(Date date) {
        Calendar calendar = getCalendar();

        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public Date getNextDate(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }


    public Date getFirstDayOfWeek(Date date) {

        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        return calendar.getTime();
    }

    public Date getLastDayOfWeek(Date date) {

        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.add(Calendar.WEEK_OF_YEAR, 1); //Move to next week
        calendar.add(Calendar.DAY_OF_MONTH, -1);//Substract one day to get last day of current week

        return calendar.getTime();
    }

    public Date getFirstDayOfMonth(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public Date getLastDayOfMonth(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    public Date getFirstDayOfYear(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    public Date getLastDayOfYear(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    @NonNull
    public static SimpleDateFormat uiDateFormat() {
        return new SimpleDateFormat(DATE_FORMAT_EXPRESSION, Locale.US);
    }

    @NonNull
    public static SimpleDateFormat timeFormat() {
        return new SimpleDateFormat("HH:mm", Locale.US);
    }

    @NonNull
    public static SimpleDateFormat dateTimeFormat() {
        return new SimpleDateFormat(DATE_TIME_FORMAT_EXPRESSION, Locale.US);
    }

    @NonNull
    public static SimpleDateFormat databaseDateFormat() {
        return new SimpleDateFormat(DATABASE_FORMAT_EXPRESSION, Locale.US);
    }
    @NonNull
    public static SimpleDateFormat databaseDateFormatNoMillis() {
        return new SimpleDateFormat(DATABASE_FORMAT_EXPRESSION_NO_MILLIS, Locale.US);
    }



    /**********************
     FORMAT REGION*/
    public String formatDate(Date dateToFormat) {
        return uiDateFormat().format(dateToFormat);
    }

    public Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**********************
     COMPARE DATES REGION*/

    public boolean hasExpired(@NonNull EventModel event, int expiryDays, int completeEventExpiryDays, @Nullable PeriodType expiryPeriodType) {
        Calendar expiredDate = Calendar.getInstance();

        if (event.completedDate() != null)
            expiredDate.setTime(event.completedDate());
        else {
            expiredDate.setTime(event.eventDate());
            expiredDate.set(Calendar.HOUR_OF_DAY, 24);
        }

        if (expiryPeriodType == null) {
            if (completeEventExpiryDays > 0)
                expiredDate.add(Calendar.DAY_OF_YEAR, completeEventExpiryDays);
            return expiredDate.getTime().before(getToday());
        } else {
            switch (expiryPeriodType) {
                case Daily:
                    break;
                case Weekly:
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1);
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.getFirstDayOfWeek());
                    break;
                case WeeklyWednesday:
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1);
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.getFirstDayOfWeek());
                    expiredDate.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                    break;
                case WeeklyThursday:
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1);
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.getFirstDayOfWeek());
                    expiredDate.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                    break;
                case WeeklySaturday:
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1);
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.getFirstDayOfWeek());
                    expiredDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                    break;
                case WeeklySunday:
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 1);
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.getFirstDayOfWeek());
                    expiredDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    break;
                case BiWeekly:
                    expiredDate.add(Calendar.WEEK_OF_YEAR, 2);
                    expiredDate.set(Calendar.DAY_OF_WEEK, expiredDate.getFirstDayOfWeek());
                    break;
                case Monthly:
                    expiredDate.add(Calendar.MONTH, 1);
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1);
                    break;
                case BiMonthly:
                    expiredDate.add(Calendar.MONTH, 2);
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1);
                    break;
                case Quarterly:
                    expiredDate.add(Calendar.MONTH, 3);
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1);
                    break;
                case SixMonthly:
                    expiredDate.add(Calendar.MONTH, 6);
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1);
                    break;
                case SixMonthlyApril:
                    expiredDate.add(Calendar.MONTH, 6);
                    expiredDate.set(Calendar.MONTH, Calendar.APRIL);
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1);
                    break;
                case Yearly:
                    expiredDate.add(Calendar.YEAR, 1);
                    expiredDate.set(Calendar.DAY_OF_YEAR, 1);
                    break;
                case FinancialApril:
                    expiredDate.add(Calendar.YEAR, 1);
                    expiredDate.set(Calendar.MONTH, Calendar.APRIL);
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1);
                    break;
                case FinancialJuly:
                    expiredDate.add(Calendar.YEAR, 1);
                    expiredDate.set(Calendar.MONTH, Calendar.JULY);
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1);
                    break;
                case FinancialOct:
                    expiredDate.add(Calendar.YEAR, 1);
                    expiredDate.add(Calendar.MONTH, Calendar.OCTOBER);
                    expiredDate.set(Calendar.DAY_OF_MONTH, 1);
                    break;
            }
            if (expiryDays > 0)
                expiredDate.add(Calendar.DAY_OF_YEAR, expiryDays);
            return expiredDate.getTime().before(getToday());
        }
    }

    public static int[] getDifference(Date startDate, Date endDate) {

        org.joda.time.Period interval = new org.joda.time.Period(startDate.getTime(), endDate.getTime(), org.joda.time.PeriodType.yearMonthDayTime());
        return new int[]{interval.getYears(), interval.getMonths(), interval.getDays()};

    }

    public Date getNewDate(List<EventModel> events, PeriodType periodType) {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        List<Date> eventDates = new ArrayList<>();
        Date newDate = new Date();
        boolean needNewDate = true;

        for (EventModel event : events) {
            eventDates.add(event.eventDate());
        }

        while (needNewDate) {
            switch (periodType) {
                case Daily:
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1); //jump one day
                    break;
                case Weekly:
                    now.setTime(moveWeekly(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1); //jump one week
                    break;
                case WeeklyWednesday:
                    now.setTime(moveWeeklyWednesday(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case WeeklyThursday:
                    now.setTime(moveWeeklyThursday(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case WeeklySaturday:
                    now.setTime(moveWeeklySaturday(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case WeeklySunday:
                    now.setTime(moveWeeklySunday(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case BiWeekly:
                    now.setTime(moveBiWeekly(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.WEEK_OF_YEAR, 2);
                    break;
                case Monthly:
                    now.setTime(moveMonthly(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.MONTH, 1);
                    break;
                case BiMonthly:
                    now.setTime(moveBiMonthly(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.MONTH, 2);
                    break;
                case Quarterly:
                    now.setTime(moveQuarterly(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.MONTH, 4);
                    break;
                case SixMonthly:
                    now.setTime(moveSixMonthly(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.MONTH, 6);
                    break;
                case SixMonthlyApril:
                    now.setTime(moveSixMonthlyApril(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.MONTH, 6);
                    break;
                case Yearly:
                    now.setTime(moveYearly(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case FinancialApril:
                    now.setTime(moveFinancialApril(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case FinancialJuly:
                    now.setTime(moveFinancialJuly(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case FinancialOct:
                    now.setTime(moveFinancialOct(now));
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                default:
                    if (!eventDates.contains(now.getTime())) {
                        newDate = now.getTime();
                        needNewDate = false;
                    }
                    now.add(Calendar.DAY_OF_YEAR, 1);
                    break;
            }

        }

        return newDate;
    }

    private Date moveWeeklyWednesday(Calendar date) {
        if (date.get(Calendar.DAY_OF_WEEK) < Calendar.WEDNESDAY)
            date.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        else {
            date.add(Calendar.WEEK_OF_YEAR, 1);
            date.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        }
        return date.getTime();
    }

    private Date moveWeeklyThursday(Calendar date) {
        if (date.get(Calendar.DAY_OF_WEEK) < Calendar.THURSDAY)
            date.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        else {
            date.add(Calendar.WEEK_OF_YEAR, 1);
            date.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        }
        return date.getTime();
    }

    private Date moveWeeklySaturday(Calendar date) {
        if (date.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY)
            date.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        else {
            date.add(Calendar.WEEK_OF_YEAR, 1);
            date.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        }
        return date.getTime();
    }

    private Date moveWeeklySunday(Calendar date) {
        if (date.get(Calendar.DAY_OF_WEEK) < Calendar.SUNDAY)
            date.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        else {
            date.add(Calendar.WEEK_OF_YEAR, 1);
            date.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
        return date.getTime();
    }

    private Date moveBiWeekly(Calendar date) {
        date.add(Calendar.WEEK_OF_YEAR, 2);
        date.set(Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek());
        return date.getTime();
    }

    private Date moveWeekly(Calendar date) {
        date.add(Calendar.WEEK_OF_YEAR, 1); //Set to next week
        date.set(Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek()); //Set to first day of next week
        date.add(Calendar.DAY_OF_YEAR, -1); //Set to last day of this week
        return date.getTime();
    }

    private Date moveMonthly(Calendar date) {
        date.add(Calendar.MONTH, 1);
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.add(Calendar.DAY_OF_MONTH, -1);
        return date.getTime();
    }

    private Date moveBiMonthly(Calendar date) {
        date.add(Calendar.MONTH, 2);
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.add(Calendar.DAY_OF_MONTH, -1);
        return date.getTime();
    }

    private Date moveQuarterly(Calendar date) {
        date.add(Calendar.MONTH, 4);
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.add(Calendar.DAY_OF_MONTH, -1);
        return date.getTime();
    }

    private Date moveSixMonthly(Calendar date) {
        date.add(Calendar.MONTH, 6);
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.add(Calendar.DAY_OF_MONTH, -1);
        return date.getTime();
    }

    private Date moveSixMonthlyApril(Calendar date) {
        if (date.get(Calendar.MONTH) > Calendar.SEPTEMBER && date.get(Calendar.MONTH) <= Calendar.DECEMBER) {
            date.add(Calendar.MONTH, 6);
            date.set(Calendar.MONTH, Calendar.APRIL);
            date.set(Calendar.DAY_OF_MONTH, 1);
        } else if (date.get(Calendar.MONTH) > Calendar.APRIL && date.get(Calendar.MONTH) < Calendar.SEPTEMBER) {
            date.set(Calendar.MONTH, Calendar.SEPTEMBER);
            date.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            date.set(Calendar.DAY_OF_MONTH, Calendar.APRIL);
            date.add(Calendar.DAY_OF_MONTH, -1);
        }
        return date.getTime();
    }

    private Date moveYearly(Calendar date) {
        date.add(Calendar.YEAR, 1);
        date.set(Calendar.DAY_OF_YEAR, 1);
        return date.getTime();
    }

    private Date moveFinancialApril(Calendar date) {
        date.add(Calendar.YEAR, 1);
        date.set(Calendar.MONTH, Calendar.APRIL);
        date.set(Calendar.DAY_OF_MONTH, 1);
        return date.getTime();
    }

    private Date moveFinancialJuly(Calendar date) {
        date.add(Calendar.YEAR, 1);
        date.set(Calendar.MONTH, Calendar.JULY);
        date.set(Calendar.DAY_OF_MONTH, 1);
        return date.getTime();
    }

    private Date moveFinancialOct(Calendar date) {
        date.add(Calendar.YEAR, 1);
        date.set(Calendar.MONTH, Calendar.OCTOBER);
        date.set(Calendar.DAY_OF_MONTH, 1);
        return date.getTime();
    }

    /**
     * @param period      Period in which the date will be selected
     * @param currentDate Current selected date
     * @param page        1 for next, 0 for now, -1 for previous
     * @return Next/Previous date calculated from the currentDate and Period
     */
    public Date getPeriodDate(PeriodType period, Date currentDate, int page) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        switch (period) {
            case Daily:
                calendar.add(Calendar.DAY_OF_YEAR, page);
                break;
            case Weekly:

                break;
            case WeeklyWednesday:

                break;
            case WeeklyThursday:

                break;
            case WeeklySaturday:

                break;
            case WeeklySunday:

                break;
            case BiWeekly:

                break;
            case Monthly:

                break;
            case BiMonthly:

                break;
            case Quarterly:

                break;
            case SixMonthly:

                break;
            case SixMonthlyApril:

                break;
            case Yearly:

                break;
            case FinancialApril:

                break;
            case FinancialJuly:

                break;
            case FinancialOct:

                break;
            default:

                break;
        }
        return calendar.getTime();
    }

    /**
     * @param currentDate      Date from which calculation will be carried out. Default value is today.
     * @param expiryDays       Number of extra days to add events on previous period
     * @param expiryPeriodType Expiry Period
     * @return Min date to select
     */
    public Date expDate(@Nullable Date currentDate, int expiryDays, @Nullable PeriodType expiryPeriodType) {

        Calendar calendar = Calendar.getInstance();

        if (currentDate != null)
            calendar.setTime(currentDate);

        Date date = calendar.getTime();

        if (expiryPeriodType == null) {
            return null;
        } else {
            switch (expiryPeriodType) {
                case Daily:
                    break;
                case Weekly:
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                    Date firstDateOfWeek = calendar.getTime();
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfWeek.getTime()) >= expiryDays) {
                        return firstDateOfWeek;
                    } else {
                        calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    }
                    break;
                case WeeklyWednesday:
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY); //moves to current week wednesday
                    Date wednesday = calendar.getTime();
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - wednesday.getTime()) >= expiryDays)
                        return wednesday;
                    else
                        calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case WeeklyThursday:
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY); //moves to current week wednesday
                    Date thursday = calendar.getTime();
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - thursday.getTime()) >= expiryDays)
                        return thursday;
                    else
                        calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case WeeklySaturday:
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); //moves to current week wednesday
                    Date saturday = calendar.getTime();
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - saturday.getTime()) >= expiryDays)
                        return saturday;
                    else
                        calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case WeeklySunday:
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); //moves to current week wednesday
                    Date sunday = calendar.getTime();
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - sunday.getTime()) >= expiryDays)
                        return sunday;
                    else
                        calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case BiWeekly:
                    if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) //if true, we are in the 2nd week of the period
                        calendar.add(Calendar.WEEK_OF_YEAR, -1);//Moved to first week
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                    Date firstDateOfBiWeek = calendar.getTime();
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfBiWeek.getTime()) >= expiryDays) {
                        return firstDateOfBiWeek;
                    } else {
                        calendar.add(Calendar.WEEK_OF_YEAR, -2);
                    }
                    break;
                case Monthly:
                    Date firstDateOfMonth = getFirstDayOfMonth(calendar.getTime());
                    calendar.setTime(firstDateOfMonth);
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfMonth.getTime()) >= expiryDays) {
                        return firstDateOfMonth;
                    } else {
                        calendar.add(Calendar.MONTH, -1);
                    }
                    break;
                case BiMonthly:
                    if (calendar.get(Calendar.MONTH) % 2 != 0) //January is 0, December is 11
                        calendar.add(Calendar.MONTH, -1); //Moved to first month
                    Date firstDateOfBiMonth = getFirstDayOfMonth(calendar.getTime());
                    calendar.setTime(firstDateOfBiMonth);
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfBiMonth.getTime()) >= expiryDays) {
                        return firstDateOfBiMonth;
                    } else {
                        calendar.add(Calendar.MONTH, -2);
                    }
                    break;
                case Quarterly:
                    while (calendar.get(Calendar.MONTH) % 4 != 0) //January is 0, December is 11
                        calendar.add(Calendar.MONTH, -1); //Moved to first month
                    Date firstDateOfQMonth = getFirstDayOfMonth(calendar.getTime());
                    calendar.setTime(firstDateOfQMonth);
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfQMonth.getTime()) >= expiryDays) {
                        return firstDateOfQMonth;
                    } else {
                        calendar.add(Calendar.MONTH, -4);
                    }
                    break;
                case SixMonthly:
                    while (calendar.get(Calendar.MONTH) % 6 != 0) //January is 0, December is 11
                        calendar.add(Calendar.MONTH, -1); //Moved to first month
                    Date firstDateOfSixMonth = getFirstDayOfMonth(calendar.getTime());
                    calendar.setTime(firstDateOfSixMonth);
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfSixMonth.getTime()) >= expiryDays) {
                        return firstDateOfSixMonth;
                    } else {
                        calendar.add(Calendar.MONTH, -6);
                    }
                    break;
                case SixMonthlyApril:
                    while ((calendar.get(Calendar.MONTH) - 3) % 6 != 0) //April is 0, December is 8
                        calendar.add(Calendar.MONTH, -1); //Moved to first month
                    Date firstDateOfSixMonthApril = getFirstDayOfMonth(calendar.getTime());
                    calendar.setTime(firstDateOfSixMonthApril);
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfSixMonthApril.getTime()) >= expiryDays) {
                        return firstDateOfSixMonthApril;
                    } else {
                        calendar.add(Calendar.MONTH, -6);
                    }
                    break;
                case Yearly:
                    Date firstDateOfYear = getFirstDayOfYear(calendar.getTime());
                    calendar.setTime(firstDateOfYear);
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfYear.getTime()) >= expiryDays) {
                        return firstDateOfYear;
                    } else {
                        calendar.add(Calendar.YEAR, -1);
                    }
                    break;
                case FinancialApril:
                    calendar.set(Calendar.MONTH, Calendar.APRIL);//Moved to April
                    Date firstDateOfAprilYear = getFirstDayOfMonth(calendar.getTime()); //first day of April
                    calendar.setTime(firstDateOfAprilYear);
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfAprilYear.getTime()) >= expiryDays) {
                        return firstDateOfAprilYear;
                    } else {
                        calendar.add(Calendar.YEAR, -1); //Moved to April last year
                    }
                    break;
                case FinancialJuly:
                    calendar.set(Calendar.MONTH, Calendar.JULY);//Moved to July
                    Date firstDateOfJulyYear = getFirstDayOfMonth(calendar.getTime()); //first day of July
                    calendar.setTime(firstDateOfJulyYear);
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfJulyYear.getTime()) >= expiryDays) {
                        return firstDateOfJulyYear;
                    } else {
                        calendar.add(Calendar.YEAR, -1); //Moved to July last year
                    }
                    break;
                case FinancialOct:
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);//Moved to October
                    Date firstDateOfOctYear = getFirstDayOfMonth(calendar.getTime()); //first day of October
                    calendar.setTime(firstDateOfOctYear);
                    if (TimeUnit.MILLISECONDS.toDays(date.getTime() - firstDateOfOctYear.getTime()) >= expiryDays) {
                        return firstDateOfOctYear;
                    } else {
                        calendar.add(Calendar.YEAR, -1); //Moved to October last year
                    }
                    break;
            }

            return calendar.getTime();
        }
    }
}
