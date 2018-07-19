package com.dhis2;

import com.dhis2.utils.DateUtils;

import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

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

        assertEquals("2018-07-15",DateUtils.uiDateFormat().format(cal.getTime()));

    }
}