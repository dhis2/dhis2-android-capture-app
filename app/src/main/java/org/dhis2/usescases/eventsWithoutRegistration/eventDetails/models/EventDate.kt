package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

data class EventDate(
    val active: Boolean = true,
    val dateValue: String? = null,
    val label: String? = null,
    val currentDate: Date? = null,
    val minDate: Date? = null,
    val maxDate: Date? = null,
    val scheduleInterval: Int = 0,
    val allowFutureDates: Boolean = true,
    val periodType: PeriodType? = null,
)
