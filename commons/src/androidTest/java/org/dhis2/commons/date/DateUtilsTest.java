package org.dhis2.commons.date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.util.Calendar;
import java.util.Date;

public class DateUtilsTest {


    @Test
    public void returnsEventOverDueDateCorrectly() {

        Calendar calendar = Calendar.getInstance();
        //should return false for current date
        calendar.setTime(new Date());
        assertFalse(DateUtils.getInstance().isEventDueDateOverdue(calendar.getTime()));
        //false for future date
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        assertFalse(DateUtils.getInstance().isEventDueDateOverdue(calendar.getTime()));
        //true for past dates
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        assertTrue(DateUtils.getInstance().isEventDueDateOverdue(calendar.getTime()));

    }
}
