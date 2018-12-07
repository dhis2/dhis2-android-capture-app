package org.dhis2;

import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.period.PeriodType;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class DateUtilsTest {

    @Test
    public void moveWeekly() throws ParseException {
        String testDate = "2018-07-13";
        Date date = DateUtils.uiDateFormat().parse(testDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(Calendar.WEEK_OF_YEAR, 1); //Set to next week
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()); //Set to first day of next week
        cal.add(Calendar.DAY_OF_YEAR, -1); //Set to last day of this week

        assertEquals("2018-07-15", DateUtils.uiDateFormat().format(cal.getTime()));
    }

    @Test
    public void moveWeeklyWednesday() throws ParseException {
        String testDate = "2018-07-23";
        Date date = DateUtils.uiDateFormat().parse(testDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        if (cal.get(Calendar.DAY_OF_WEEK) < Calendar.WEDNESDAY) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            assertEquals("2018-07-25", DateUtils.uiDateFormat().format(cal.getTime()));

        } else {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            assertEquals("2018-07-25", DateUtils.uiDateFormat().format(cal.getTime()));
        }
    }

//    @Test
//    public void expiryPeriodAndDaysOutOfRange() throws ParseException {
//        String testDateOutOfRange = "2018-08-01";
//
//        String[] expectedResults = new String[]{
//                "2018-08-01",//Daily
//                "2018-07-30",//Weekly
//                "2018-07-25",//WeeklyWednesday
//                "2018-07-26",//WeeklyThursday
//                "2018-07-28",//WeeklySaturday
//                "2018-07-29",//WeeklySunday
//                "2018-07-30",//BiWeekly
//                "2018-07-01",//Monthly
//                "2018-07-01",//BiMonthly
//                "2018-05-01",//Quarterly
//                "2018-07-01",//SixMonthly
//                "2018-04-01",//SixMonthlyApril
//                "2018-01-01",//Yearly
//                "2018-04-01",//FinancialApril
//                "2018-07-01",//FinancialJuly
//                "2017-10-01"};//FinancialOct
//
//        Date dateOutOfRange = DateUtils.uiDateFormat().parse(testDateOutOfRange);
//
//        int expiryDays = 2;
//
//        int i = 0;
//        for (PeriodType period : PeriodType.values()) {
//            Date minDate = DateUtils.getInstance().expDate(dateOutOfRange, expiryDays, period);
//
//            assertEquals(expectedResults[i], DateUtils.uiDateFormat().format(minDate));
//            i++;
//        }
//    }

    @Test
    public void expiryPeriodAndDaysInRange() throws ParseException {
        String testDateInRange = "2018-07-31";
        Date dateInRange = DateUtils.uiDateFormat().parse(testDateInRange);

        PeriodType periodType = PeriodType.Weekly;
        int expiryDays = 2;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dateInRange);

        Date minDate = DateUtils.getInstance().expDate(dateInRange, expiryDays, periodType);

        assertEquals("2018-07-23", DateUtils.uiDateFormat().format(minDate));
    }

    @Test
    public void getNextPeriod() throws ParseException {
        String currentDate = "2018-09-13";

        String[] expectedResults = new String[]{
                "2018-09-14",//Daily
                "2018-09-17",//Weekly
                "2018-09-19",//WeeklyWednesday
                "2018-09-20",//WeeklyThursday
                "2018-09-22",//WeeklySaturday
                "2018-09-23",//WeeklySunday
                "2018-09-24",//BiWeekly
                "2018-10-01",//Monthly
                "2018-11-01",//BiMonthly
                "2019-01-01",//Quarterly
                "2019-01-01",//SixMonthly
                "2018-10-01",//SixMonthlyApril
                "2019-01-01",//Yearly
                "2019-04-01",//FinancialApril
                "2019-07-01",//FinancialJuly
                "2018-10-01"};//FinancialOct

        Date testDate = DateUtils.uiDateFormat().parse(currentDate);
        int i = 0;
        for (PeriodType period : PeriodType.values()) {
            Date minDate = DateUtils.getInstance().getNextPeriod(period, testDate, 1);

            assertEquals(expectedResults[i], DateUtils.uiDateFormat().format(minDate));
            i++;
        }
    }

    @Test
    public void isEventExpired() throws ParseException {
        String completedDate = "2018-09-13";
        int compExpDays = 2;
        String[] currentDates = new String[]{
                "2018-09-14",
                "2018-09-17",
                "2018-09-15",
                "2018-09-11"
        };
        Boolean[] expectedResults = new Boolean[]{
                false,
                true,
                false,
                false
        };

        Date testDate = DateUtils.uiDateFormat().parse(completedDate);


        int i = 0;
        for (String date : currentDates) {
            Boolean isExpired = DateUtils.getInstance().isEventExpired(
                    DateUtils.uiDateFormat().parse(date), testDate, compExpDays);
            assertEquals(expectedResults[i], isExpired);
            i++;
        }
    }

    @Test
    public void getDateFromDateAndPeriod() throws ParseException {
        DateUtils dateUtils = DateUtils.getInstance();

        String dateToTestString = "2018-12-05";
        String firstDayOfYearString = "2018-01-01";
        String lastDayOfYearString = "2018-12-31";
        Date dateToTest = DateUtils.uiDateFormat().parse(dateToTestString);
        Date firstDayOfYear = DateUtils.uiDateFormat().parse(firstDayOfYearString);
        Date lastDayOfYear = DateUtils.uiDateFormat().parse(lastDayOfYearString);

        Date[] dates1 = dateUtils.getDateFromDateAndPeriod(dateToTest, Period.YEARLY);
        assertEquals(dates1[0], firstDayOfYear);
        assertEquals(dates1[1], lastDayOfYear);

        String firstDayOfMonthString = "2018-12-01";
        String lastDayOfMonthString = "2018-12-31";
        Date firstDayOfMonth = DateUtils.uiDateFormat().parse(firstDayOfMonthString);
        Date lastDayOfMonth = DateUtils.uiDateFormat().parse(lastDayOfMonthString);

        Date[] dates2 = dateUtils.getDateFromDateAndPeriod(dateToTest, Period.MONTHLY);
        assertEquals(dates2[0], firstDayOfMonth);
        assertEquals(dates2[1], lastDayOfMonth);

        String firstDayOfWeekString = "2018-12-03";
        String lastDayOfWeekString = "2018-12-09";
        Date firstDayOfWeek = DateUtils.uiDateFormat().parse(firstDayOfWeekString);
        Date lastDayOfWeek = DateUtils.uiDateFormat().parse(lastDayOfWeekString);

        Date[] dates3 = dateUtils.getDateFromDateAndPeriod(dateToTest, Period.WEEKLY);
        assertEquals(dates3[0], firstDayOfWeek);
        assertEquals(dates3[1], lastDayOfWeek);

        String currentDayString = "2018-12-05";
        String nextDayString = "2018-12-06";
        Date currentDay = DateUtils.uiDateFormat().parse(currentDayString);
        Date nextDay = DateUtils.uiDateFormat().parse(nextDayString);

        Date[] dates4 = dateUtils.getDateFromDateAndPeriod(dateToTest, Period.DAILY);
        assertEquals(dates4[0], currentDay);
        assertEquals(dates4[1], nextDay);
    }

    @Test
    public void testPeriodNames() {
        assertEquals(R.string.period, Period.NONE.getNameResouce());
        assertEquals(R.string.DAILY, Period.DAILY.getNameResouce());
        assertEquals(R.string.WEEKLY, Period.WEEKLY.getNameResouce());
        assertEquals(R.string.MONTHLY, Period.MONTHLY.getNameResouce());
        assertEquals(R.string.YEARLY, Period.YEARLY.getNameResouce());
    }

    @Test
    public void testTimeFormats() {
        SimpleDateFormat uiFormat = new SimpleDateFormat(DateUtils.DATE_FORMAT_EXPRESSION, Locale.US);
        assertEquals(uiFormat, DateUtils.uiDateFormat());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        assertEquals(timeFormat, DateUtils.timeFormat());

        SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT_EXPRESSION, Locale.US);
        assertEquals(dateFormat, DateUtils.dateTimeFormat());

        SimpleDateFormat databaseDateFormat = new SimpleDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION, Locale.US);
        assertEquals(databaseDateFormat, DateUtils.databaseDateFormat());
    }

    @Test
    public void testGetToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        assertEquals(calendar.getTime(), DateUtils.getInstance().getToday());
    }

    @Test
    public void testGetDifference() throws ParseException {
        String startDateString = "2017-11-04";
        String endDateString = "2018-12-05";
        Date startDate = DateUtils.uiDateFormat().parse(startDateString);
        Date endDate = DateUtils.uiDateFormat().parse(endDateString);

        int[] diff = DateUtils.getDifference(startDate, endDate);
        assertEquals(1, diff[0]);
        assertEquals(1, diff[1]);
        assertEquals(1, diff[2]);
    }

    @Test
    public void moveWeekWednesday() throws ParseException {
        String dateString = "2018-12-04";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2018-12-05";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        String date3String = "2018-12-06";
        Date date3 = DateUtils.uiDateFormat().parse(date3String);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTime(date3);

        String date4String = "2018-12-12";
        Date date4 = DateUtils.uiDateFormat().parse(date4String);
        Calendar calendar4 = Calendar.getInstance();
        calendar4.setTime(date4);


        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveWeeklyWednesday(calendar));
        assertEquals(calendar4.getTime(), DateUtils.getInstance().moveWeeklyWednesday(calendar2));
        assertEquals(calendar4.getTime(), DateUtils.getInstance().moveWeeklyWednesday(calendar3));
    }
}