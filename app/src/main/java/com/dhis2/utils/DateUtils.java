package com.dhis2.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.period.PeriodModel;
import org.hisp.dhis.android.core.period.PeriodType;
import org.joda.time.Days;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by ppajuelo on 16/01/2018.
 */

public class DateUtils {

    public static DateUtils getInstance() {
        return new DateUtils();
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public Date[] getDateFromPeriod(Period period) {
        switch (period) {
            case YEARLY:
                return new Date[]{getFirstDayOfCurrentYear(), getLastDayOfCurrentYear()};
            case MONTHLY:
                return new Date[]{getFirstDayOfurrentMonth(), getLastDayOfurrentMonth()};
            case WEEKLY:
                return new Date[]{getFirstDayOfCurrentWeek(), getLastDayOfCurrentWeek()};
            case DAILY:
            default:
                return new Date[]{getToday(), getToday()};
        }
    }

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
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    @NonNull
    public static SimpleDateFormat timeFormat() {
        return new SimpleDateFormat("HH:mm", Locale.US);
    }

    @NonNull
    public static SimpleDateFormat dateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    }

    @NonNull
    public static SimpleDateFormat databaseDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
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

    public boolean hasExpired(@NonNull Date completedDate, int expiryDays, int completeEventExpiryDays, @Nullable PeriodType expiryPeriodType){
        Calendar expiredDate = Calendar.getInstance();
        expiredDate.setTime(completedDate);

        if(expiryPeriodType == null){
            if(completeEventExpiryDays > 0)
                expiredDate.add(Calendar.DAY_OF_YEAR, completeEventExpiryDays);
            return expiredDate.getTime().before(getToday());
        } else {
            switch (expiryPeriodType){
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
            if(expiryDays > 0)
                expiredDate.add(Calendar.DAY_OF_YEAR, expiryDays);
            return expiredDate.getTime().before(getToday());
        }
    }

    public Date toDate(String date) {

        try {
            return databaseDateFormat().parse(date);

        } catch (ParseException e) {
            Timber.e(e);
            return null;
        }
    }

    public static int[] getDifference(Date startDate, Date endDate) {

        org.joda.time.Period interval = new org.joda.time.Period(startDate.getTime(), endDate.getTime());
        return new int[]{interval.getYears(), interval.getMonths(), interval.getDays()};

    }
}
