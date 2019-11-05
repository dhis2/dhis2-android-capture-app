package org.dhis2.utils;

import org.dhis2.R;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DateUtilsTest {

    @Test
    public void contentsAreTheSame() {
        TrackedEntityAttributeValue attributeValue1 = TrackedEntityAttributeValue.builder()
                .value("10")
                .trackedEntityInstance("tei")
                .trackedEntityAttribute("attribute")
                .build();

        TrackedEntityAttributeValue attributeValue3 = TrackedEntityAttributeValue.builder()
                .value("10")
                .trackedEntityInstance("tei")
                .trackedEntityAttribute("attribute")
                .build();

        TrackedEntityAttributeValue attributeValue2 = TrackedEntityAttributeValue.builder()
                .value("11")
                .trackedEntityInstance("tei")
                .trackedEntityAttribute("attribute")
                .build();

        assertTrue(!attributeValue1.equals(attributeValue2));
        assertEquals(attributeValue1, attributeValue3);
    }

    @Ignore
    @Test
    public void moveWeekly() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2018-12-09";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveWeekly(calendar));
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

    @Ignore
    @Test
    public void expiryPeriodAndDaysInRange() throws ParseException {
        String testDateInRange = "2018-07-31";
        Date dateInRange = DateUtils.uiDateFormat().parse(testDateInRange);

        String testDateInRange2 = "2018-07-01";
        Date dateInRange2 = DateUtils.uiDateFormat().parse(testDateInRange2);

        String testDateInRange3 = "2018-08-01";
        Date dateInRange3 = DateUtils.uiDateFormat().parse(testDateInRange3);

        String testDateInRange4 = "2018-06-01";
        Date dateInRange4 = DateUtils.uiDateFormat().parse(testDateInRange4);


        Calendar cal = Calendar.getInstance();
        cal.setTime(dateInRange);

        Date nullDate = DateUtils.getInstance().expDate(dateInRange, 2, null);
        Date minDateWeekly = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.Weekly);
        Date minDateWeekly2 = DateUtils.getInstance().expDate(dateInRange, 1, PeriodType.Weekly);
        Date minDateDaily = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.Daily);
        Date minDateWeeklyWednesday = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.WeeklyWednesday);
        Date minDateWeeklyWednesday2 = DateUtils.getInstance().expDate(dateInRange, -1, PeriodType.WeeklyWednesday);
        Date minDateWeeklyThursday = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.WeeklyThursday);
        Date minDateWeeklyThursday2 = DateUtils.getInstance().expDate(dateInRange, -2, PeriodType.WeeklyThursday);
        Date minDateWeeklySaturday = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.WeeklySaturday);
        Date minDateWeeklySaturday2 = DateUtils.getInstance().expDate(dateInRange, -4, PeriodType.WeeklySaturday);
        Date minDateWeeklySunday = DateUtils.getInstance().expDate(dateInRange, 2, PeriodType.WeeklySunday);
        Date minDateWeeklySunday2 = DateUtils.getInstance().expDate(dateInRange, -5, PeriodType.WeeklySunday);
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
        Date minDateFinancialOct2 = DateUtils.getInstance().expDate(dateInRange, -100, PeriodType.FinancialOct);

        assertEquals(null, nullDate);
        assertEquals("2018-07-23", DateUtils.uiDateFormat().format(minDateWeekly));
        assertEquals("2018-07-30", DateUtils.uiDateFormat().format(minDateWeekly2));
        assertEquals("2018-07-29", DateUtils.uiDateFormat().format(minDateDaily));
        assertEquals("2018-07-25", DateUtils.uiDateFormat().format(minDateWeeklyWednesday));
        assertEquals("2018-08-01", DateUtils.uiDateFormat().format(minDateWeeklyWednesday2));
        assertEquals("2018-07-26", DateUtils.uiDateFormat().format(minDateWeeklyThursday));
        assertEquals("2018-08-02", DateUtils.uiDateFormat().format(minDateWeeklyThursday2));
        assertEquals("2018-07-28", DateUtils.uiDateFormat().format(minDateWeeklySaturday));
        assertEquals("2018-08-04", DateUtils.uiDateFormat().format(minDateWeeklySaturday2));
        assertEquals("2018-07-29", DateUtils.uiDateFormat().format(minDateWeeklySunday));
//        assertEquals("2018-07-29", DateUtils.uiDateFormat().format(minDateWeeklySunday2));
        assertEquals("2018-07-29", DateUtils.uiDateFormat().format(minDateBiWeekly));
        assertEquals("2018-06-17", DateUtils.uiDateFormat().format(minDateBiWeekly2));
        assertEquals("2018-07-01", DateUtils.uiDateFormat().format(minDateMonthly));
        assertEquals("2018-06-01", DateUtils.uiDateFormat().format(minDateMonthly2));
        assertEquals("2018-07-01", DateUtils.uiDateFormat().format(minDateBiMonthly));
        assertEquals("2018-05-01", DateUtils.uiDateFormat().format(minDateBiMonthly2));
        assertEquals("2018-05-01", DateUtils.uiDateFormat().format(minDateQuarterly));
        assertEquals("2018-01-01", DateUtils.uiDateFormat().format(minDateQuarterly2));
        assertEquals("2018-07-01", DateUtils.uiDateFormat().format(minDateSixMonthly));
        assertEquals("2017-07-01", DateUtils.uiDateFormat().format(minDateSixMonthly2));
        assertEquals("2018-04-01", DateUtils.uiDateFormat().format(minDateSixMonthlyApril));
        assertEquals("2017-10-01", DateUtils.uiDateFormat().format(minDateSixMonthlyApril2));
        assertEquals("2018-01-01", DateUtils.uiDateFormat().format(minDateYearly));
        assertEquals("2017-01-01", DateUtils.uiDateFormat().format(minDateYearly2));
        assertEquals("2018-05-01", DateUtils.uiDateFormat().format(minDateFinancialApril));
        assertEquals("2017-05-01", DateUtils.uiDateFormat().format(minDateFinancialApril2));
        assertEquals("2018-07-01", DateUtils.uiDateFormat().format(minDateFinancialJuly));
        assertEquals("2017-07-01", DateUtils.uiDateFormat().format(minDateFinancialJuly2));
        assertEquals("2017-10-01", DateUtils.uiDateFormat().format(minDateFinancialOct));
        assertEquals("2018-10-01", DateUtils.uiDateFormat().format(minDateFinancialOct2));
    }

    @Ignore
    @Test
    public void getNextPeriod() throws ParseException {
        String currentDate = "2018-09-13";
        String currentDate2 = "2018-02-13";
        String currentDate3 = "2018-12-13";

        String[] expectedResults = new String[]{
                "2018-09-14",//Daily
                "2018-09-17",//Weekly
                "2018-09-19",//WeeklyWednesday
                "2018-09-20",//WeeklyThursday
                "2018-09-22",//WeeklySaturday
                "2018-09-16",//WeeklySunday
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
        Date testDate2 = DateUtils.uiDateFormat().parse(currentDate2);
        Date testDate3 = DateUtils.uiDateFormat().parse(currentDate3);

        int i = 0;
        for (PeriodType period : PeriodType.values()) {
            Date minDate = DateUtils.getInstance().getNextPeriod(period, testDate, 1);
            assertEquals(expectedResults[i], DateUtils.uiDateFormat().format(minDate));
            i++;
        }
        // test null period - default is daily
        Date minDate = DateUtils.getInstance().getNextPeriod(null, testDate, 1);
        assertEquals(expectedResults[0], DateUtils.uiDateFormat().format(minDate));

        // test special cases
        Date minDate2 = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthlyApril, testDate2, 1);
        assertEquals("2018-04-01", DateUtils.uiDateFormat().format(minDate2));

        Date minDate3 = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialApril, testDate2, 1);
        assertEquals("2018-04-01", DateUtils.uiDateFormat().format(minDate3));

        Date minDate4 = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialJuly, testDate2, 1);
        assertEquals("2018-07-01", DateUtils.uiDateFormat().format(minDate4));

        Date minDate5 = DateUtils.getInstance().getNextPeriod(PeriodType.SixMonthlyApril, testDate3, 1);
        assertEquals("2019-04-01", DateUtils.uiDateFormat().format(minDate5));

        Date minDate6 = DateUtils.getInstance().getNextPeriod(PeriodType.FinancialOct, testDate3, 1);
        assertEquals("2019-10-01", DateUtils.uiDateFormat().format(minDate6));
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

    @Ignore
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

        String firstDayOfWeekString = "2018-12-02";
        String lastDayOfWeekString = "2018-12-08";
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
        Assert.assertEquals(R.string.period, Period.NONE.getNameResouce());
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
        Date startDate = DateUtils.uiDateFormat().parse(startDateString);
        Date endDate = DateUtils.uiDateFormat().parse(endDateString);

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

    @Test
    public void moveWeekThursday() throws ParseException {
        String dateString = "2018-12-05";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2018-12-06";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        String date3String = "2018-12-07";
        Date date3 = DateUtils.uiDateFormat().parse(date3String);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTime(date3);

        String date4String = "2018-12-13";
        Date date4 = DateUtils.uiDateFormat().parse(date4String);
        Calendar calendar4 = Calendar.getInstance();
        calendar4.setTime(date4);


        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveWeeklyThursday(calendar));
        assertEquals(calendar4.getTime(), DateUtils.getInstance().moveWeeklyThursday(calendar2));
        assertEquals(calendar4.getTime(), DateUtils.getInstance().moveWeeklyThursday(calendar3));
    }

    @Ignore
    @Test
    public void moveWeekSaturday() throws ParseException {
        String dateString = "2018-12-07";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2018-12-08";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        String date3String = "2018-12-09";
        Date date3 = DateUtils.uiDateFormat().parse(date3String);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTime(date3);

        String date4String = "2018-12-22";
        Date date4 = DateUtils.uiDateFormat().parse(date4String);
        Calendar calendar4 = Calendar.getInstance();
        calendar4.setTime(date4);

        String date5String = "2018-12-15";
        Date date5 = DateUtils.uiDateFormat().parse(date5String);
        Calendar calendar5 = Calendar.getInstance();
        calendar5.setTime(date5);


        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveWeeklySaturday(calendar));
        assertEquals(calendar5.getTime(), DateUtils.getInstance().moveWeeklySaturday(calendar2));
        assertEquals(calendar4.getTime(), DateUtils.getInstance().moveWeeklySaturday(calendar3));
    }

    @Ignore
    @Test
    public void moveWeekSunday() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2018-12-02";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        String date3String = "2018-12-10";
        Date date3 = DateUtils.uiDateFormat().parse(date3String);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTime(date3);

        String date4String = "2018-12-09";
        Date date4 = DateUtils.uiDateFormat().parse(date4String);
        Calendar calendar4 = Calendar.getInstance();
        calendar4.setTime(date4);


        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveWeeklySunday(calendar));
        assertEquals(calendar4.getTime(), DateUtils.getInstance().moveWeeklySunday(calendar2));
        assertEquals(calendar4.getTime(), DateUtils.getInstance().moveWeeklySunday(calendar3));
    }

    @Ignore
    @Test
    public void moveBiWeekly() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2018-12-16";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveBiWeekly(calendar));
    }

    @Test
    public void moveMonthly() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2018-12-31";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveMonthly(calendar));
    }

    @Test
    public void moveBiMonthly() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2019-01-31";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveBiMonthly(calendar));
    }

    @Test
    public void moveQuarterly() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2019-03-31";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveQuarterly(calendar));
    }

    @Test
    public void moveSixMonthly() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2019-05-31";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveSixMonthly(calendar));
    }

    @Test
    public void moveSixMonthlyApril() throws ParseException {
        String dateString = "2018-10-01";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2018-05-01";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        String date3String = "2018-09-01";
        Date date3 = DateUtils.uiDateFormat().parse(date3String);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTime(date3);

        String date4String = "2018-04-01";
        Date date4 = DateUtils.uiDateFormat().parse(date4String);
        Calendar calendar4 = Calendar.getInstance();
        calendar4.setTime(date4);


        String date11String = "2019-04-01";
        Date date11 = DateUtils.uiDateFormat().parse(date11String);
        Calendar calendar11 = Calendar.getInstance();
        calendar11.setTime(date11);

        String date12String = "2018-04-01";
        Date date12 = DateUtils.uiDateFormat().parse(date12String);
        Calendar calendar12 = Calendar.getInstance();
        calendar12.setTime(date12);

        String date21String = "2018-09-01";
        Date date21 = DateUtils.uiDateFormat().parse(date21String);
        Calendar calendar21 = Calendar.getInstance();
        calendar21.setTime(date21);

        assertEquals(calendar11.getTime(), DateUtils.getInstance().moveSixMonthlyApril(calendar));
        assertEquals(calendar12.getTime(), DateUtils.getInstance().moveSixMonthlyApril(calendar4));

        assertEquals(calendar21.getTime(), DateUtils.getInstance().moveSixMonthlyApril(calendar2));
        assertEquals(calendar21.getTime(), DateUtils.getInstance().moveSixMonthlyApril(calendar3));
    }

    @Test
    public void moveYearly() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2019-01-01";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveYearly(calendar));
    }

    @Test
    public void moveFinancialApril() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2019-04-01";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveFinancialApril(calendar));
    }

    @Test
    public void moveFinancialJuly() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2019-07-01";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveFinancialJuly(calendar));
    }

    @Test
    public void moveFinancialOct() throws ParseException {
        String dateString = "2018-12-08";
        Date date = DateUtils.uiDateFormat().parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String date2String = "2019-10-01";
        Date date2 = DateUtils.uiDateFormat().parse(date2String);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        assertEquals(calendar2.getTime(), DateUtils.getInstance().moveFinancialOct(calendar));
    }

    @Ignore
    @Test
    public void testGetPeriodUIString() throws ParseException {
        Date testDate = DateUtils.uiDateFormat().parse("2019-01-11");

        assertEquals("2019-01-11", DateUtils.getInstance().getPeriodUIString(null, testDate, Locale.ENGLISH));
        assertEquals("2019-01-11", DateUtils.getInstance().getPeriodUIString(PeriodType.Daily, testDate, Locale.ENGLISH));
        assertEquals("2 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.Weekly, testDate, Locale.ENGLISH));
        assertEquals("2 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.WeeklyWednesday, testDate, Locale.ENGLISH));
        assertEquals("2 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.WeeklyThursday, testDate, Locale.ENGLISH));
        assertEquals("2 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.WeeklySaturday, testDate, Locale.ENGLISH));
        assertEquals("2 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.WeeklySunday, testDate, Locale.ENGLISH));
        assertEquals("2 2019 - 3 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.BiWeekly, testDate, Locale.ENGLISH));
        assertEquals("Jan 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.Monthly, testDate, Locale.ENGLISH));
        assertEquals("Jan 2019 - Feb 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.BiMonthly, testDate, Locale.ENGLISH));
        assertEquals("Jan 2019 - Apr 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.Quarterly, testDate, Locale.ENGLISH));
        assertEquals("Jan 2019 - Jun 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.SixMonthly, testDate, Locale.ENGLISH));
        assertEquals("Oct 2018 - Mar 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.SixMonthlyApril, testDate, Locale.ENGLISH));
        assertEquals("2019", DateUtils.getInstance().getPeriodUIString(PeriodType.Yearly, testDate, Locale.ENGLISH));
        assertEquals("Apr 2018 - Mar 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.FinancialApril, testDate, Locale.ENGLISH));
        assertEquals("Jul 2018 - Jun 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.FinancialJuly, testDate, Locale.ENGLISH));
        assertEquals("Oct 2018 - Sep 2019", DateUtils.getInstance().getPeriodUIString(PeriodType.FinancialOct, testDate, Locale.ENGLISH));
    }

    private Date toDate(String date) throws ParseException {
        return DateUtils.uiDateFormat().parse(date);
    }

    @Test
    public void active_event_NcD_NPT_NeD_is_not_expired() throws ParseException {

        Date currentDate = DateUtils.uiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(!DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.ACTIVE, 0, null, 0));
        assertTrue(!DateUtils.getInstance().isEventExpired(toDate("2019-03-02"), null, EventStatus.ACTIVE, 0, null, 0));
        assertTrue(!DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), null, EventStatus.ACTIVE, 0, null, 0));

    }

    @Ignore
    @Test
    public void active_event_NcD_Monthly_0_is_expired() throws ParseException {

        Date currentDate = DateUtils.uiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), null, EventStatus.ACTIVE, 0, PeriodType.Monthly, 0));

    }

    @Ignore
    @Test
    public void active_event_NcD_Monthly_1_is_expired() throws ParseException {

        Date currentDate = DateUtils.uiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(!DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.ACTIVE, 0, PeriodType.Monthly, 1));
        assertTrue(!DateUtils.getInstance().isEventExpired(toDate("2019-03-02"), null, EventStatus.ACTIVE, 0, PeriodType.Monthly, 1));
        assertTrue(!DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), null, EventStatus.ACTIVE, 0, PeriodType.Monthly, 1));

        currentDate = DateUtils.uiDateFormat().parse("2019-03-02");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.ACTIVE, 0, null, 0));
    }

    @Test
    public void complete_event_NcD_NPT_NeD_is_not_expired() throws ParseException {

        Date currentDate = DateUtils.uiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(!DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.COMPLETED, 0, null, 0));

    }

    @Test
    public void complete_event_1_NPT_NeD_is_not_expired() throws ParseException {

        Date currentDate = DateUtils.uiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(!DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), null, EventStatus.COMPLETED, 0, null, 1));

    }

    @Ignore
    @Test
    public void complete_event_1_Monthly_0_is_expired() throws ParseException {

        Date currentDate = DateUtils.uiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), toDate("2019-03-01"), EventStatus.COMPLETED, 1, PeriodType.Monthly, 0));

    }

    @Test
    public void complete_event_1_Monthly_1_is_not_expired() throws ParseException {

        Date currentDate = DateUtils.uiDateFormat().parse("2019-03-01");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(!DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), toDate("2019-02-28"), EventStatus.COMPLETED, 1, PeriodType.Monthly, 1));

    }

    @Ignore
    @Test
    public void complete_event_1_NPT_NeD_is_expired() throws ParseException {

        Date currentDate = DateUtils.uiDateFormat().parse("2019-03-02");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(DateUtils.getInstance().isEventExpired(toDate("2019-03-01"), toDate("2019-03-01"), EventStatus.COMPLETED, 1, null, 0));

    }

    @Test
    public void complete_event_1_Monthly_1_is_expired() throws ParseException {

        Date currentDate = DateUtils.uiDateFormat().parse("2019-03-02");
        DateUtils.getInstance().setCurrentDate(currentDate);

        assertTrue(DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), toDate("2019-03-01"), EventStatus.COMPLETED, 1, null, 0));

    }

    @Ignore
    @Test(expected = NullPointerException.class)
    public void complete_event_with_null_complete_date_throws_error() throws ParseException {

        DateUtils.getInstance().isEventExpired(toDate("2019-02-28"), null, EventStatus.COMPLETED, 1, null, 0);

    }
}