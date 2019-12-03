package org.dhis2.Bindings

import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import java.util.Date

fun Event.primaryDate() : Date {

    return when(status()){
        EventStatus.ACTIVE -> eventDate()!!
        EventStatus.COMPLETED -> eventDate()!!
        EventStatus.SCHEDULE -> dueDate()!!
        EventStatus.SKIPPED -> dueDate()!!
        EventStatus.VISITED -> eventDate()!!
        EventStatus.OVERDUE -> dueDate()!!
        null -> Date()
    }

}