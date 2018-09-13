package org.dhis2;

import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.period.PeriodType;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

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

    @Test
    public void expiryPeriodAndDaysOutOfRange() throws ParseException {
        String testDateOutOfRange = "2018-08-01";

        String[] expectedResults = new String[]{
                "2018-08-01",//Daily
                "2018-07-30",//Weekly
                "2018-07-25",//WeeklyWednesday
                "2018-07-26",//WeeklyThursday
                "2018-07-28",//WeeklySaturday
                "2018-07-29",//WeeklySunday
                "2018-07-30",//BiWeekly
                "2018-07-01",//Monthly
                "2018-07-01",//BiMonthly
                "2018-05-01",//Quarterly
                "2018-07-01",//SixMonthly
                "2018-04-01",//SixMonthlyApril
                "2018-01-01",//Yearly
                "2018-04-01",//FinancialApril
                "2018-07-01",//FinancialJuly
                "2017-10-01"};//FinancialOct

        Date dateOutOfRange = DateUtils.uiDateFormat().parse(testDateOutOfRange);

        int expiryDays = 2;

        int i = 0;
        for (PeriodType period : PeriodType.values()) {
            Date minDate = DateUtils.getInstance().expDate(dateOutOfRange, expiryDays, period);

            assertEquals(expectedResults[i], DateUtils.uiDateFormat().format(minDate));
            i++;
        }

    }

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
}