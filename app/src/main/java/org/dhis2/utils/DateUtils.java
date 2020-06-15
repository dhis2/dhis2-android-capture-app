package org.dhis2.utils;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.section.viewmodels.date.DatePickerDialogFragment;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.customviews.RxDateDialog;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.dataset.DataInputPeriod;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.period.PeriodType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 16/01/2018.
 */

public class DateUtils {

    private static DateUtils instance;
    private Calendar currentDateCalendar;

    public static DateUtils getInstance() {
        if (instance == null)
            instance = new DateUtils();

        return instance;
    }

    public static final String DATABASE_FORMAT_EXPRESSION = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String DATABASE_FORMAT_EXPRESSION_NO_MILLIS = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATABASE_FORMAT_EXPRESSION_NO_SECONDS = "yyyy-MM-dd'T'HH:mm";
    public static final String DATE_TIME_FORMAT_EXPRESSION = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_EXPRESSION = "yyyy-MM-dd";

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
        return getCalendar().getTime();
    }

    /**********************
     SELECTED PEDIOD REGION*/

    private Date getDate(Date date) {
        Calendar calendar = getCalendar();

        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private Date getNextDate(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }


    private Date getFirstDayOfWeek(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        return calendar.getTime();
    }

    private Date getLastDayOfWeek(Date date) {
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

    private Date getFirstDayOfMonth(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    private Date getLastDayOfMonth(Date date) {
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

    private Date getFirstDayOfYear(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    private Date getLastDayOfYear(Date date) {
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

    @NonNull
    public static SimpleDateFormat databaseDateFormatNoSeconds() {
        return new SimpleDateFormat(DATABASE_FORMAT_EXPRESSION_NO_SECONDS, Locale.US);
    }

    @Nonnull
    public static Boolean dateHasNoSeconds(String dateTime) {
        try {
            databaseDateFormatNoSeconds().parse(dateTime);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }


    /**********************
     FORMAT REGION*/
    public String formatDate(Date dateToFormat) {
        return uiDateFormat().format(dateToFormat);
    }

    public Calendar getCalendar() {
        if (currentDateCalendar != null)
            return currentDateCalendar;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public void setCurrentDate(Date date) {
        currentDateCalendar = getCalendar();
        currentDateCalendar.setTime(date);
        currentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentDateCalendar.set(Calendar.MINUTE, 0);
        currentDateCalendar.set(Calendar.SECOND, 0);
        currentDateCalendar.set(Calendar.MILLISECOND, 0);
    }

    /**********************
     COMPARE DATES REGION*/

    public static int[] getDifference(Date startDate, Date endDate) {
        org.joda.time.Period interval = new org.joda.time.Period(startDate.getTime(), endDate.getTime(), org.joda.time.PeriodType.yearMonthDayTime());
        return new int[]{interval.getYears(), interval.getMonths(), interval.getDays()};
    }

    /**
     * @param currentDate      Date from which calculation will be carried out. Default value is today.
     * @param expiryDays       Number of extra days to add events on previous period
     * @param expiryPeriodType Expiry Period
     * @return Min date to select
     */
    public Date expDate(@Nullable Date currentDate, int expiryDays, @Nullable PeriodType expiryPeriodType) {

        Calendar calendar = getCalendar();

        if (currentDate != null)
            calendar.setTime(currentDate);

        Date date = calendar.getTime();

        if (expiryPeriodType == null) {
            return null;
        } else {
            switch (expiryPeriodType) {
                case Daily:
                    calendar.add(Calendar.DAY_OF_YEAR, -expiryDays);
                    return calendar.getTime();
                case Weekly:
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
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

    /**
     * @param period      Period in which the date will be selected
     * @param currentDate Current selected date
     * @param page        1 for next, 0 for now, -1 for previous
     * @return Next/Previous date calculated from the currentDate and Period
     */
    public Date getNextPeriod(PeriodType period, Date currentDate, int page) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        int extra;
        if (period == null)
            period = PeriodType.Daily;

        switch (period) {
            case Daily:
                calendar.add(Calendar.DAY_OF_YEAR, page);
                break;
            case Weekly:
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case WeeklyWednesday:
                calendar.setFirstDayOfWeek(Calendar.WEDNESDAY);
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                break;
            case WeeklyThursday:
                calendar.setFirstDayOfWeek(Calendar.THURSDAY);
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                break;
            case WeeklySaturday:
                calendar.setFirstDayOfWeek(Calendar.SATURDAY);
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                break;
            case WeeklySunday:
                calendar.setFirstDayOfWeek(Calendar.SUNDAY);
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
            case BiWeekly:
                extra = calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0 ? 1 : 2;
                calendar.add(Calendar.WEEK_OF_YEAR, page * extra);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case Monthly:
                calendar.add(Calendar.MONTH, page);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case BiMonthly:
                extra = (calendar.get(Calendar.MONTH) + 1) % 2 == 0 ? 1 : 2;
                calendar.add(Calendar.MONTH, page * extra);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case Quarterly:
                extra = 3 * page - (calendar.get(Calendar.MONTH) % 3);
                calendar.add(Calendar.MONTH, extra);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case SixMonthly:
                extra = 6 * page - (calendar.get(Calendar.MONTH) % 6);
                calendar.add(Calendar.MONTH, extra);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case SixMonthlyApril:
                if (calendar.get(Calendar.MONTH) < Calendar.APRIL) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);
                } else if (calendar.get(Calendar.MONTH) >= Calendar.APRIL && calendar.get(Calendar.MONTH) < Calendar.OCTOBER)
                    calendar.set(Calendar.MONTH, Calendar.APRIL);
                else
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, page * 6);
                break;
            case SixMonthlyNov:
                if (calendar.get(Calendar.MONTH) < Calendar.MAY) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
                } else if (calendar.get(Calendar.MONTH) >= Calendar.MAY && calendar.get(Calendar.MONTH) < Calendar.NOVEMBER)
                    calendar.set(Calendar.MONTH, Calendar.MAY);
                else
                    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, page * 6);
                break;
            case Yearly:
                calendar.add(Calendar.YEAR, page);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
            case FinancialApril:
                if (calendar.get(Calendar.MONTH) < Calendar.APRIL) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.APRIL);
                } else
                    calendar.set(Calendar.MONTH, Calendar.APRIL);

                calendar.add(Calendar.YEAR, page);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case FinancialJuly:
                if (calendar.get(Calendar.MONTH) < Calendar.JULY) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.JULY);
                } else
                    calendar.set(Calendar.MONTH, Calendar.JULY);
                calendar.add(Calendar.YEAR, page);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case FinancialOct:
                if (calendar.get(Calendar.MONTH) < Calendar.OCTOBER) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);
                } else
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);

                calendar.add(Calendar.YEAR, page);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case FinancialNov:
                if (calendar.get(Calendar.MONTH) < Calendar.NOVEMBER) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
                } else
                    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);

                calendar.add(Calendar.YEAR, page);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            default:
                break;
        }
        return calendar.getTime();
    }

    /**
     * @param period      Period in which the date will be selected
     * @param currentDate Current selected date
     * @param page        1 for next, 0 for now, -1 for previous
     * @return Next/Previous date calculated from the currentDate and Period
     */
    public Date getNextPeriod(PeriodType period, Date currentDate, int page, boolean lastDate) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        int extra;
        if (period == null)
            period = PeriodType.Daily;

        switch (period) {
            case Daily:
                calendar.add(Calendar.DAY_OF_YEAR, page);
                break;
            case Weekly:
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
            case WeeklyWednesday:
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                break;
            case WeeklyThursday:
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case WeeklySaturday:
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                break;
            case WeeklySunday:
                calendar.add(Calendar.WEEK_OF_YEAR, page);
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                break;
            case BiWeekly:
                extra = calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0 ? 1 : 2;
                calendar.add(Calendar.WEEK_OF_YEAR, page * extra);
                if (!lastDate)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                else
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
            case Monthly:
                calendar.add(Calendar.MONTH, page);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case BiMonthly:
                extra = (calendar.get(Calendar.MONTH) + 1) % 2 == 0 ? 1 : 2;
                calendar.add(Calendar.MONTH, page * extra);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case Quarterly:
                extra = 1 + 4 - (calendar.get(Calendar.MONTH) + 1) % 4;
                calendar.add(Calendar.MONTH, page * extra);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case SixMonthly:
                extra = 1 + 6 - (calendar.get(Calendar.MONTH) + 1) % 6;
                calendar.add(Calendar.MONTH, page * extra);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case SixMonthlyApril:
                if (calendar.get(Calendar.MONTH) < Calendar.APRIL) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);
                } else if (calendar.get(Calendar.MONTH) >= Calendar.APRIL && calendar.get(Calendar.MONTH) < Calendar.OCTOBER)
                    calendar.set(Calendar.MONTH, Calendar.APRIL);
                else
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, page * 6);
                break;
            case Yearly:
                calendar.add(Calendar.YEAR, page);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
            case FinancialApril:
                if (calendar.get(Calendar.MONTH) < Calendar.APRIL) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.APRIL);
                } else
                    calendar.set(Calendar.MONTH, Calendar.APRIL);

                calendar.add(Calendar.YEAR, page);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case FinancialJuly:
                if (calendar.get(Calendar.MONTH) < Calendar.JULY) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.JULY);
                } else
                    calendar.set(Calendar.MONTH, Calendar.JULY);
                calendar.add(Calendar.YEAR, page);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case FinancialOct:
                if (calendar.get(Calendar.MONTH) < Calendar.OCTOBER) {
                    calendar.add(Calendar.YEAR, -1);
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);
                } else
                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);

                calendar.add(Calendar.YEAR, page);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            default:
                break;
        }
        return calendar.getTime();
    }

    public String getPeriodUIString(PeriodType periodType, Date date, Locale locale) {

        String formattedDate;
        Date initDate = getNextPeriod(periodType, date, 0);

        Calendar cal = getCalendar();
        cal.setTime(getNextPeriod(periodType, date, 1));
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date endDate = cal.getTime();
        String periodString = "%s - %s";
        if (periodType == null)
            periodType = PeriodType.Daily;
        switch (periodType) {
            case Weekly:
            case WeeklyWednesday:
            case WeeklyThursday:
            case WeeklySaturday:
            case WeeklySunday:
                Calendar endWeek = Calendar.getInstance();
                endWeek.setTime(initDate);
                endWeek.add(Calendar.DAY_OF_MONTH, 6);
                String DATE_LABEL_FORMAT = "Week %s to %s";
                formattedDate = String.format(DATE_LABEL_FORMAT, new SimpleDateFormat("w yyyy-MM-dd", locale).format(initDate),
                        new SimpleDateFormat(" yyyy-MM-dd", locale).format(endWeek.getTime()));
                break;
            case BiWeekly:
                formattedDate = String.format(periodString,
                        new SimpleDateFormat("w yyyy", locale).format(initDate),
                        new SimpleDateFormat("w yyyy", locale).format(endDate)
                );
                break;
            case Monthly:
                formattedDate = new SimpleDateFormat("MMM yyyy", locale).format(initDate);
                break;
            case BiMonthly:
            case Quarterly:
            case SixMonthly:
            case SixMonthlyApril:
            case FinancialApril:
            case FinancialJuly:
            case FinancialOct:
                formattedDate = String.format(periodString,
                        new SimpleDateFormat("MMM yyyy", locale).format(initDate),
                        new SimpleDateFormat("MMM yyyy", locale).format(endDate)
                );
                break;
            case Yearly:
                formattedDate = new SimpleDateFormat("yyyy", locale).format(initDate);
                break;
            case Daily:
            default:
                formattedDate = uiDateFormat().format(initDate);
                break;
        }

        return formattedDate;
    }

    /**
     * Check if an event is expired in a date
     *
     * @param currentDate  date or today if null
     * @param completedDay date that event was completed
     * @param compExpDays  days of expiration of an event
     * @return true or false
     */
    public Boolean isEventExpired(@Nullable Date currentDate, Date completedDay, int compExpDays) {

        Calendar calendar = getCalendar();

        if (currentDate != null)
            calendar.setTime(currentDate);

        Date date = calendar.getTime();

        return completedDay != null && compExpDays > 0 &&
                completedDay.getTime() + TimeUnit.DAYS.toMillis(compExpDays) < date.getTime();
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
    public Boolean isEventExpired(Date eventDate, Date completeDate, EventStatus status, int compExpDays, PeriodType programPeriodType, int expDays) {
        if (status == EventStatus.COMPLETED && completeDate == null)
            return false;

        boolean expiredBecouseOfPeriod;
        boolean expiredBecouseOfCompletion = false;

        expiredBecouseOfCompletion = status == EventStatus.COMPLETED ?
                isEventExpired(null, eventDate, compExpDays) : false;

        if (programPeriodType != null) {
            Date expDate = getNextPeriod(programPeriodType, eventDate, 1); //Initial date of next period
            Date currentDate = getCalendar().getTime();
            if (expDays > 0) {
                Calendar calendar = getCalendar();
                calendar.setTime(expDate);
                calendar.add(Calendar.DAY_OF_YEAR, expDays);
                expDate = calendar.getTime();
            }
            expiredBecouseOfPeriod = expDate != null && expDate.compareTo(currentDate)<=0;

            return expiredBecouseOfPeriod || expiredBecouseOfCompletion;
        } else
            return expiredBecouseOfCompletion;

    }


    public Boolean isDataSetExpired(int expiredDays, Date periodInitialDate) {
        return Calendar.getInstance().getTime().getTime() > periodInitialDate.getTime() + TimeUnit.DAYS.toMillis(expiredDays);
    }

    public Boolean isInsideInputPeriod(DataInputPeriod dataInputPeriodModel) {
        if (dataInputPeriodModel.openingDate() == null && dataInputPeriodModel.closingDate() != null)
            return Calendar.getInstance().getTime().getTime() < dataInputPeriodModel.closingDate().getTime();

        if (dataInputPeriodModel.openingDate() != null && dataInputPeriodModel.closingDate() == null)
            return dataInputPeriodModel.openingDate().getTime() < Calendar.getInstance().getTime().getTime();

        if (dataInputPeriodModel.openingDate() == null && dataInputPeriodModel.closingDate() == null)
            return true;

        return dataInputPeriodModel.openingDate().getTime() < Calendar.getInstance().getTime().getTime()
                && Calendar.getInstance().getTime().getTime() < dataInputPeriodModel.closingDate().getTime();
    }

    public String generateId(PeriodType periodType, Date date, Locale locale) {

        String formattedDate;
        Date initDate = getNextPeriod(periodType, date, 0);

        switch (periodType) {
            case Monthly:
                formattedDate = new SimpleDateFormat("yyyyMM", locale).format(initDate);
                break;
            case Yearly:
                formattedDate = new SimpleDateFormat("yyyy", locale).format(initDate);
                break;
            case Daily:
                formattedDate = new SimpleDateFormat("yyyyMMdd", locale).format(initDate);
                break;
            default:
                formattedDate = new SimpleDateFormat("yyyy", locale).format(initDate);
                break;
        }
        return formattedDate;
    }

    public List<DatePeriod> getDatePeriodListFor(List<Date> selectedDates, Period period) {
        List<DatePeriod> datePeriods = new ArrayList<>();
        for (Date date : selectedDates) {
            Date[] startEndDates = getDateFromDateAndPeriod(date, period);
            datePeriods.add(DatePeriod.builder().startDate(startEndDates[0]).endDate(startEndDates[1]).build());
        }
        return datePeriods;
    }

    public void showFromToSelector(ActivityGlobalAbstract activity, OnFromToSelector fromToListener) {
        DatePickerDialogFragment fromCalendar = DatePickerDialogFragment.create(true);
        if (!FilterManager.getInstance().getPeriodFilters().isEmpty())
            fromCalendar.setInitialDate(FilterManager.getInstance().getPeriodFilters().get(0).startDate());
        fromCalendar.setFormattedOnDateSetListener(new DatePickerDialogFragment.FormattedOnDateSetListener() {
            @Override
            public void onDateSet(@NonNull Date fromDate) {
                DatePickerDialogFragment toCalendar = DatePickerDialogFragment.create(true);
                toCalendar.setOpeningClosingDates(fromDate, null);
                toCalendar.setFormattedOnDateSetListener(new DatePickerDialogFragment.FormattedOnDateSetListener() {
                    @Override
                    public void onDateSet(@NonNull Date toDate) {
                        List<DatePeriod> list = new ArrayList<>();
                        list.add(DatePeriod.builder().startDate(fromDate).endDate(toDate).build());
                        fromToListener.onFromToSelected(list);
                    }

                    @Override
                    public void onClearDate() {

                    }
                });
                toCalendar.show(activity.getSupportFragmentManager(), "TO");

            }

            @Override
            public void onClearDate() {

            }
        });

        fromCalendar.show(activity.getSupportFragmentManager(), "FROM");
    }

    public void showPeriodDialog(ActivityGlobalAbstract activity, OnFromToSelector fromToListener, boolean fromOtherPeriod) {
        DatePickerDialogFragment fromCalendar = DatePickerDialogFragment.create(true, "Daily", fromOtherPeriod);
//        fromCalendar.setOpeningClosingDates(null, null); TODO: MAX 1 year in the future?
        if (!FilterManager.getInstance().getPeriodFilters().isEmpty())
            fromCalendar.setInitialDate(FilterManager.getInstance().getPeriodFilters().get(0).startDate());
        fromCalendar.setFormattedOnDateSetListener(new DatePickerDialogFragment.FormattedOnDateSetListener() {
            @Override
            public void onDateSet(@NonNull Date date) {
                fromToListener.onFromToSelected(getDatePeriodListFor(Collections.singletonList(date), Period.DAILY));
            }

            @Override
            public void onClearDate() {
                Disposable disposable = new RxDateDialog(activity, Period.WEEKLY)
                        .createForFilter().show()
                        .subscribe(
                                selectedDates -> fromToListener.onFromToSelected(getDatePeriodListFor(selectedDates.val1(),
                                        selectedDates.val0())),
                                Timber::e
                        );
            }
        });
        fromCalendar.show(activity.getSupportFragmentManager(), "DAILY");

    }


    public static long timeToDate(Date finaLDate) {
        return finaLDate.getTime() - new Date().getTime();
    }

    public interface OnFromToSelector {
        void onFromToSelected(List<DatePeriod> datePeriods);
    }
}
