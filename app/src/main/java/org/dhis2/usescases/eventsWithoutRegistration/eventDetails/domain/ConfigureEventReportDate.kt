package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import java.util.Calendar.DAY_OF_YEAR
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.ADDNEW
import org.dhis2.commons.data.EventCreationType.DEFAULT
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.date.DateUtils
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage

class ConfigureEventReportDate(
    private val creationType: EventCreationType = DEFAULT,
    private val resourceProvider: EventDetailResourcesProvider,
    private val repository: EventDetailsRepository,
    private val periodType: PeriodType? = null,
    private val periodUtils: DhisPeriodUtils,
    private val enrollmentId: String? = null,
    private val scheduleInterval: Int = 0
) {

    operator fun invoke(selectedDate: Date? = null): Flow<EventDate> {
        return flowOf(
            EventDate(
                active = isActive(),
                label = getLabel(),
                dateValue = getDateValue(selectedDate),
                currentDate = getDate(selectedDate),
                minDate = getMinDate(),
                maxDate = getMaxDate(),
                scheduleInterval = getScheduleInterval(),
                allowFutureDates = getAllowFutureDates(),
                periodType = periodType
            )
        )
    }

    private fun isActive(): Boolean {
        if (creationType == SCHEDULE && getProgramStage().hideDueDate() == true) {
            return false
        }
        return true
    }

    private fun getLabel(): String {
        val programStage = getProgramStage()
        return when (creationType) {
            SCHEDULE ->
                programStage.dueDateLabel() ?: resourceProvider.provideDueDate()
            else -> {
                programStage.executionDateLabel() ?: resourceProvider.provideEventDate()
            }
        }
    }

    private fun getDate(selectedDate: Date?) = when {
        selectedDate != null -> selectedDate
        repository.getEvent() != null -> repository.getEvent()?.eventDate()
        periodType != null -> getDateBasedOnPeriodType()
        creationType == SCHEDULE -> getNextScheduleDate()
        else -> getCurrentDay()
    }

    private fun getDateValue(selectedDate: Date?) = getDate(selectedDate)?.let { date ->
        when {
            periodType != null ->
                periodUtils.getPeriodUIString(periodType, date, Locale.getDefault())
            else -> DateUtils.uiDateFormat().format(date)
        }
    }

    private fun getProgramStage(): ProgramStage = repository.getProgramStage()

    private fun getDateBasedOnPeriodType(): Date {
        getProgramStage().hideDueDate()?.let { hideDueDate ->
            if (creationType == SCHEDULE && hideDueDate) {
                return if (periodType != null) {
                    DateUtils.getInstance().today
                } else {
                    val calendar = DateUtils.getInstance().calendar
                    calendar.add(DAY_OF_YEAR, getScheduleInterval())
                    org.dhis2.utils.DateUtils.getInstance().getNextPeriod(
                        null,
                        calendar.time,
                        0
                    )
                }
            }
        }

        return DateUtils.getInstance()
            .getNextPeriod(
                periodType,
                DateUtils.getInstance().today,
                if (creationType != SCHEDULE) 0 else 1
            )
    }

    private fun getNextScheduleDate(): Date {
        val isGeneratedEventBasedOnEnrollment =
            repository.getProgramStage().generatedByEnrollmentDate()

        val initialDate = if (isGeneratedEventBasedOnEnrollment == true) {
            val enrollmentDate = repository.getEnrollmentDate(enrollmentId)
            DateUtils.getInstance().getCalendarByDate(enrollmentDate)
        } else {
            val date = DateUtils.getInstance().calendar
            date.time = repository.getStageLastDate(enrollmentId)
            date
        }

        val minDateFromStart =
            repository.getMinDaysFromStartByProgramStage()
        if (minDateFromStart > 0) {
            initialDate.add(DAY_OF_YEAR, minDateFromStart)
        }
        return DateUtils.getInstance().getNextPeriod(null, initialDate.time, 0)
    }

    private fun getCurrentDay() = DateUtils.getInstance().today

    private fun getMinDate(): Date? {
        repository.getProgram()?.let { program ->
            if (periodType == null) {
                if (program.expiryPeriodType() != null) {
                    val expiryDays = program.expiryDays() ?: 0
                    return org.dhis2.utils.DateUtils.getInstance().expDate(
                        null,
                        expiryDays,
                        program.expiryPeriodType()
                    )
                }
            } else {
                var minDate = org.dhis2.utils.DateUtils.getInstance().expDate(
                    null,
                    program.expiryDays() ?: 0,
                    periodType
                )
                val lastPeriodDate = org.dhis2.utils.DateUtils.getInstance().getNextPeriod(
                    periodType,
                    minDate,
                    -1,
                    true
                )

                if (lastPeriodDate.after(
                        org.dhis2.utils.DateUtils.getInstance().getNextPeriod(
                                program.expiryPeriodType(),
                                minDate,
                                0
                            )
                    )
                ) {
                    minDate = org.dhis2.utils.DateUtils.getInstance()
                        .getNextPeriod(periodType, lastPeriodDate, 0)
                }
                return minDate
            }
        }
        return null
    }

    private fun getMaxDate(): Date? {
        return if (periodType == null) {
            when (creationType) {
                ADDNEW,
                DEFAULT -> Date(System.currentTimeMillis() - 1000)
                else -> null
            }
        } else {
            when (creationType) {
                ADDNEW,
                DEFAULT -> DateUtils.getInstance().today
                else -> null
            }
        }
    }

    private fun getAllowFutureDates() = when (creationType) {
        SCHEDULE -> true
        else -> false
    }

    private fun getScheduleInterval() = when (creationType) {
        SCHEDULE -> scheduleInterval
        else -> 0
    }
}
