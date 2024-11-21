package org.dhis2.commons.date;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.Calendar;
import java.util.Date;

public class DateUtilsTest {


    @Test
    public void returnsEventOverDueDateCorrectly() {

        Calendar calendar = Calendar.getInstance();
        //should return false for current date
        calendar.setTime(new Date());
        assertEquals(false, DateUtils.getInstance().isEventDueDateOverdue(calendar.getTime()));
        //false for future date
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        assertEquals(false, DateUtils.getInstance().isEventDueDateOverdue(calendar.getTime()));
        //true for past dates
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        assertEquals(true, DateUtils.getInstance().isEventDueDateOverdue(calendar.getTime()));

    }
}
