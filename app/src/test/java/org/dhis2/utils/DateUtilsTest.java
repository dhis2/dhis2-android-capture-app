package org.dhis2.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.dhis2.R;
import org.dhis2.bindings.StringExtensionsKt;
import org.dhis2.commons.date.DateUtils;
import org.dhis2.commons.date.Period;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.PeriodType;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtilsTest {

    @Test
    public void expiryPeriodAndDaysInRange() throws ParseException {
        Locale.setDefault(new Locale("es", "ES"));

        String testDateInRange = "2018-07-31";
        Date dateInRange = DateUtils.oldUiDateFormat().parse(testDateInRange);

        String testDateInRange2 = "2018-07-01";
        Date dateInRange2 = DateUtils.oldUiDateFormat().parse(testDateInRange2);

        String testDateInRange3 = "2018-08-01";
        Date dateInRange3 = DateUtils.oldUiDateFormat().parse(testDateInRange3);

        String testDateInRange4 = "2018-06-01";
        Date dateInRange4 = DateUtils.oldUiDateFormat().parse(testDateInRange4);


        Calendar cal = Calendar.getInstance();
        cal.setTime(dateInRange);
        DateUtils.getInstance().setCurrentDate(dateInRange);
        DateUtils.getInstance().getCalendar().setFirstDayOfWeek(Calendar.MONDAY);

        Date nullDate = DateUtils.getInstance().expDate(dateInRange, 2, null);
        Date minDateWeekly = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.Weekly);
        Date minDateWeekly2 = DateUtils.getInstance().expDate(dateInRange, 1, PeriodType.Weekly);
        Date minDateDaily = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.Daily);
        Date minDateWeeklyWednesday = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.WeeklyWednesday);
        Date minDateWeeklyThursday = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.WeeklyThursday);
        Date minDateWeeklySaturday = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.WeeklySaturday);
        Date minDateWeeklySunday = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.WeeklySunday);
        Date minDateBiWeekly = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.BiWeekly);
        Date minDateBiWeekly2 = DateUtils.getInstance().expDate(dateInRange2, 3, PeriodType.BiWeekly);
        Date minDateMonthly = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.Monthly);
        Date minDateMonthly2 = DateUtils.getInstance().expDate(dateInRange, 100, PeriodType.Monthly);
        Date minDateBiMonthly = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.BiMonthly);
        Date minDateBiMonthly2 = DateUtils.getInstance().expDate(dateInRange3, 100, PeriodType.BiMonthly);
        Date minDateQuarterly = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.Quarterly);
        Date minDateQuarterly2 = DateUtils.getInstance().expDate(dateInRange, 100, PeriodType.Quarterly);
        Date minDateSixMonthly = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.SixMonthly);
        Date minDateSixMonthly2 = DateUtils.getInstance().expDate(dateInRange4, 500, PeriodType.SixMonthly);
        Date minDateSixMonthlyApril = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.SixMonthlyApril);
        Date minDateSixMonthlyApril2 = DateUtils.getInstance().expDate(dateInRange, 500, PeriodType.SixMonthlyApril);
        Date minDateYearly = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.Yearly);
        Date minDateYearly2 = DateUtils.getInstance().expDate(dateInRange, 500, PeriodType.Yearly);
        Date minDateFinancialApril = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.FinancialApril);
        Date minDateFinancialApril2 = DateUtils.getInstance().expDate(dateInRange, 100, PeriodType.FinancialApril);
        Date minDateFinancialJuly = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.FinancialJuly);
        Date minDateFinancialJuly2 = DateUtils.getInstance().expDate(dateInRange, 100, PeriodType.FinancialJuly);
        Date minDateFinancialOct = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.FinancialOct);

        assertNull(nullDate);
        assertEquals("2018-07-23", DateUtils.oldUiDateFormat().format(minDateWeekly));
        assertEquals("2018-07-30", DateUtils.oldUiDateFormat().format(minDateWeekly2));
        assertEquals("2018-07-29", DateUtils.oldUiDateFormat().format(minDateDaily));
        assertEquals("2018-07-25", DateUtils.oldUiDateFormat().format(minDateWeeklyWednesday));
        assertEquals("2018-07-26", DateUtils.oldUiDateFormat().format(minDateWeeklyThursday));
        assertEquals("2018-07-28", DateUtils.oldUiDateFormat().format(minDateWeeklySaturday));
        assertEquals("2018-07-29", DateUtils.oldUiDateFormat().format(minDateWeeklySunday));
        assertEquals("2018-07-16", DateUtils.oldUiDateFormat().format(minDateBiWeekly));
        assertEquals("2018-06-18", DateUtils.oldUiDateFormat().format(minDateBiWeekly2));
        assertEquals("2018-07-01", DateUtils.oldUiDateFormat().format(minDateMonthly));
        assertEquals("2018-06-01", DateUtils.oldUiDateFormat().format(minDateMonthly2));
        assertEquals("2018-07-01", DateUtils.oldUiDateFormat().format(minDateBiMonthly));
        assertEquals("2018-05-01", DateUtils.oldUiDateFormat().format(minDateBiMonthly2));
        assertEquals("2018-05-01", DateUtils.oldUiDateFormat().format(minDateQuarterly));
        assertEquals("2018-01-01", DateUtils.oldUiDateFormat().format(minDateQuarterly2));
        assertEquals("2018-07-01", DateUtils.oldUiDateFormat().format(minDateSixMonthly));
        assertEquals("2017-07-01", DateUtils.oldUiDateFormat().format(minDateSixMonthly2));
        assertEquals("2018-04-01", DateUtils.oldUiDateFormat().format(minDateSixMonthlyApril));
        assertEquals("2017-10-01", DateUtils.oldUiDateFormat().format(minDateSixMonthlyApril2));
        assertEquals("2018-01-01", DateUtils.oldUiDateFormat().format(minDateYearly));
        assertEquals("2017-01-01", DateUtils.oldUiDateFormat().format(minDateYearly2));
        assertEquals("2018-05-01", DateUtils.oldUiDateFormat().format(minDateFinancialApril));
        assertEquals("2017-05-01", DateUtils.oldUiDateFormat().format(minDateFinancialApril2));
        assertEquals("2018-07-01", DateUtils.oldUiDateFormat().format(minDateFinancialJuly));
        assertEquals("2017-07-01", DateUtils.oldUiDateFormat().format(minDateFinancialJuly2));
        assertEquals("2017-10-01", DateUtils.oldUiDateFormat().format(minDateFinancialOct));
    }


    @Test
    public void getPeriodDaily() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-09-12";
        String expectedCurrentDate = "2018-09-13";
        String expectedNextDate = "2018-09-14";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.Daily, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.Daily, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.Daily, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodWeekly() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-09-03";
        String expectedCurrentDate = "2018-09-10";
        String expectedNextDate = "2018-09-17";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.Weekly, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.Weekly, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.Weekly, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodWeeklyWednesday() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-09-05";
        String expectedCurrentDate = "2018-09-12";
        String expectedNextDate = "2018-09-19";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklyWednesday, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklyWednesday, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklyWednesday, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodWeeklyThursday() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-09-06";
        String expectedCurrentDate = "2018-09-13";
        String expectedNextDate = "2018-09-20";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklyThursday, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklyThursday, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklyThursday, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodWeeklySaturday() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-09-01";
        String expectedCurrentDate = "2018-09-08";
        String expectedNextDate = "2018-09-15";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklySaturday, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklySaturday, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklySaturday, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodWeeklySunday() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-09-02";
        String expectedCurrentDate = "2018-09-09";
        String expectedNextDate = "2018-09-16";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklySunday, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklySunday, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.WeeklySunday, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodBiWeekly() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-08-27";
        String expectedCurrentDate = "2018-09-10";
        String expectedNextDate = "2018-09-24";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.BiWeekly, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.BiWeekly, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.BiWeekly, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodMonthly() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-08-01";
        String expectedCurrentDate = "2018-09-01";
        String expectedNextDate = "2018-10-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.Monthly, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.Monthly, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.Monthly, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodBiMonthly() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-07-01";
        String expectedCurrentDate = "2018-09-01";
        String expectedNextDate = "2018-11-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.BiMonthly, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.BiMonthly, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.BiMonthly, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodQuarterly() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-04-01";
        String expectedCurrentDate = "2018-07-01";
        String expectedNextDate = "2018-10-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.Quarterly, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.Quarterly, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.Quarterly, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodSixMonthly() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2018-01-01";
        String expectedCurrentDate = "2018-07-01";
        String expectedNextDate = "2019-01-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthly, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthly, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthly, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodSixMonthlyApril() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2017-10-01";
        String expectedCurrentDate = "2018-04-01";
        String expectedNextDate = "2018-10-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthlyApril, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthlyApril, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthlyApril, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodSixMonthlyNov() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2017-11-01";
        String expectedCurrentDate = "2018-05-01";
        String expectedNextDate = "2018-11-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthlyNov, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthlyNov, testDate, 0);
        Date nxtDate = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthlyNov, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nxtDate));
    }

    @Test
    public void getNextPeriodYearly() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2017-01-01";
        String expectedCurrentDate = "2018-01-01";
        String expectedNextDate = "2019-01-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.Yearly, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.Yearly, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.Yearly, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodFinancialApril() throws ParseException {
        Date testDate = StringExtensionsKt.toDate("2018-09-13");
        String expectedPrevDate = "2017-04-01";
        String expectedCurrentDate = "2018-04-01";
        String expectedNextDate = "2019-04-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialApril, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialApril, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialApril, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodFinancialJuly() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2017-07-01";
        String expectedCurrentDate = "2018-07-01";
        String expectedNextDate = "2019-07-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialJuly, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialJuly, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialJuly, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodFinancialOct() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2016-10-01";
        String expectedCurrentDate = "2017-10-01";
        String expectedNextDate = "2018-10-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialOct, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialOct, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialOct, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
    }

    @Test
    public void getNextPeriodFinancialNov() throws ParseException {
        Date testDate = DateUtils.oldUiDateFormat().parse("2018-09-13");
        String expectedPrevDate = "2016-11-01";
        String expectedCurrentDate = "2017-11-01";
        String expectedNextDate = "2018-11-01";

        Date prevDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialNov, testDate, -1);
        Date currentDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialNov, testDate, 0);
        Date nextDate = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialNov, testDate, 1);
        assertEquals(expectedPrevDate, DateUtils.oldUiDateFormat().format(prevDate));
        assertEquals(expectedCurrentDate, DateUtils.oldUiDateFormat().format(currentDate));
        assertEquals(expectedNextDate, DateUtils.oldUiDateFormat().format(nextDate));
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

        Date testDate = DateUtils.oldUiDateFormat().parse(completedDate);


        int i = 0;
        for (String date : currentDates) {
            Boolean isExpired = DateUtils.getInstance().isEventExpired(
                    DateUtils.oldUiDateFormat().parse(date), testDate, compExpDays);
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
        Date dateToTest = DateUtils.oldUiDateFormat().parse(dateToTestString);
        Date firstDayOfYear = DateUtils.oldUiDateFormat().parse(firstDayOfYearString);
        Date lastDayOfYear = DateUtils.oldUiDateFormat().parse(lastDayOfYearString);
        dateUtils.setCurrentDate(dateToTest);
        dateUtils.getCalendar().setFirstDayOfWeek(Calendar.MONDAY);

        Date[] dates1 = dateUtils.getDateFromDateAndPeriod(dateToTest, Period.YEARLY);
        assertEquals(dates1[0], firstDayOfYear);
        assertEquals(dates1[1], lastDayOfYear);

        String firstDayOfMonthString = "2018-12-01";
        String lastDayOfMonthString = "2018-12-31";
        Date firstDayOfMonth = DateUtils.oldUiDateFormat().parse(firstDayOfMonthString);
        Date lastDayOfMonth = DateUtils.oldUiDateFormat().parse(lastDayOfMonthString);

        Date[] dates2 = dateUtils.getDateFromDateAndPeriod(dateToTest, Period.MONTHLY);
        assertEquals(dates2[0], firstDayOfMonth);
        assertEquals(dates2[1], lastDayOfMonth);

        String firstDayOfWeekString = "2018-12-03";
        String lastDayOfWeekString = "2018-12-09";
        Date firstDayOfWeek = DateUtils.oldUiDateFormat().parse(firstDayOfWeekString);
        Date lastDayOfWeek = DateUtils.oldUiDateFormat().parse(lastDayOfWeekString);

        Date[] dates3 = dateUtils.getDateFromDateAndPeriod(dateToTest, Period.WEEKLY);
        assertEquals(dates3[0], firstDayOfWeek);
        assertEquals(dates3[1], lastDayOfWeek);

        String currentDayString = "2018-12-05";
        Date currentDay = DateUtils.oldUiDateFormat().parse(currentDayString);

        Date[] dates4 = dateUtils.getDateFromDateAndPeriod(dateToTest, Period.DAILY);
        assertEquals(dates4[0], currentDay);
        assertEquals(dates4[1], currentDay);
    }

    @Test
    public void testPeriodNames() {
        Assert.assertEquals(R.string.period, Period.NONE.getNameResouce());
        assertEquals(R.string.DAILY, Period.DAILY.getNameResouce());
        assertEquals(R.string.WEEKLY, Period.WEEKLY.getNameResouce());
        assertEquals(R.string.MONTHLY, Period.MONTHLY.getNameResouce());
        assertEquals(R.string.YEARLY, Period.YEARLY.getNameResouce());
    }

    @Test
    public void testTimeFormats() {
        SimpleDateFormat uiFormat = new SimpleDateFormat(DateUtils.SIMPLE_DATE_FORMAT, Locale.US);
        assertEquals(uiFormat, DateUtils.uiDateFormat());

        SimpleDateFormat oldUiFormat = new SimpleDateFormat(DateUtils.DATE_FORMAT_EXPRESSION, Locale.US);
        assertEquals(oldUiFormat, DateUtils.oldUiDateFormat());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        assertEquals(timeFormat, DateUtils.timeFormat());

        SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT_EXPRESSION, Locale.US);
        assertEquals(dateFormat, DateUtils.dateTimeFormat());

        SimpleDateFormat databaseDateFormat = new SimpleDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION, Locale.US);
        assertEquals(databaseDateFormat, DateUtils.databaseDateFormat());

        SimpleDateFormat databaseDateNoMillisFormat = new SimpleDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION_NO_MILLIS, Locale.US);
        assertEquals(databaseDateNoMillisFormat, DateUtils.databaseDateFormatNoMillis());

        SimpleDateFormat databaseDateNoSecondsFormat = new SimpleDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION_NO_SECONDS, Locale.US);
        assertEquals(databaseDateNoSecondsFormat, DateUtils.databaseDateFormatNoSeconds());
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
        String startDateString = "2006-01-24";
        String endDateString = "2019-01-23";
        Date startDate = DateUtils.oldUiDateFormat().parse(startDateString);
        Date endDate = DateUtils.oldUiDateFormat().parse(endDateString);

        int[] diff = DateUtils.getDifference(startDate, endDate);
        assertEquals(12, diff[0]);
        assertEquals(11, diff[1]);
        assertEquals(30, diff[2]);
    }

    @Test
    public void testFormatDate() {
        DateUtils dateUtils = DateUtils.getInstance();
        Date dateToFormat = Calendar.getInstance().getTime();
        String dateFormatted = DateUtils.uiDateFormat().format(dateToFormat);

        assertEquals(dateFormatted, dateUtils.formatDate(dateToFormat));
    }

    private Date toDate(String date) throws ParseException {
        return DateUtils.oldUiDateFormat().parse(date);
    }

    @Test
    public void active_event_NcD_NPT_NeD_is_not_expired() throws ParseException {

        Date currentDate = DateUtils.oldUiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.ACTIVE, 0, null, 0));
        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-03-02"), null, EventStatus.ACTIVE, 0, null, 0));
        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), null, EventStatus.ACTIVE, 0, null, 0));

    }


    @Test
    public void active_event_NcD_Monthly_0_is_expired() throws ParseException {

        Date currentDate = DateUtils.oldUiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), null, EventStatus.ACTIVE, 0, PeriodType.Monthly, 0));

    }


    @Test
    public void active_event_NcD_Monthly_1_is_expired() throws ParseException {

        Date currentDate = DateUtils.oldUiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.ACTIVE, 0, PeriodType.Monthly, 1));
        DateUtils.getInstance().setCurrentDate(currentDate);
        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-03-02"), null, EventStatus.ACTIVE, 0, PeriodType.Monthly, 1));
        DateUtils.getInstance().setCurrentDate(currentDate);
        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), null, EventStatus.ACTIVE, 0, PeriodType.Monthly, 1));

        currentDate = DateUtils.oldUiDateFormat().parse("2019-03-02");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.ACTIVE, 0, null, 0));
    }

    @Test
    public void complete_event_NcD_NPT_NeD_is_not_expired() throws ParseException {

        Date currentDate = DateUtils.oldUiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.COMPLETED, 0, null, 0));

    }

    @Test
    public void complete_event_1_NPT_NeD_is_not_expired() throws ParseException {

        Date currentDate = DateUtils.oldUiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.COMPLETED, 0, null, 1));

    }


    @Test
    public void complete_event_1_Monthly_0_is_expired() throws ParseException {

        Date currentDate = DateUtils.oldUiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), toDate("2019-03-01"), EventStatus.COMPLETED, 1, PeriodType.Monthly, 0));

    }


    @Test
    public void complete_event_1_Monthly_1_is_not_expired() throws ParseException {

        Date currentDate = DateUtils.oldUiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), toDate("2019-02-28"), EventStatus.COMPLETED, 1, PeriodType.Monthly, 1));

    }


    @Test
    public void complete_event_1_NPT_NeD_is_expired() throws ParseException {

        Date currentDate = DateUtils.oldUiDateFormat().parse("2019-03-02");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), toDate("2019-03-01"), EventStatus.COMPLETED, 1, null, 0));

    }

    @Test
    public void complete_event_1_Monthly_1_is_expired() throws ParseException {

        Date currentDate = DateUtils.oldUiDateFormat().parse("2019-03-02");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), toDate("2019-03-01"), EventStatus.COMPLETED, 1, null, 0));

    }


    @Test
    public void complete_event_with_null_complete_date_throws_error() throws ParseException {

        assertFalse(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), null, EventStatus.COMPLETED, 1, null, 0));

    }

    @Test
    public void shouldParseDate() {
        String testDate = "2022-01-01'T'12:01:01.001";
        Date date = StringExtensionsKt.toDate(testDate);
        assertNotNull(date);
    }
}