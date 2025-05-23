package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.ADDNEW
import org.dhis2.commons.data.EventCreationType.DEFAULT
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Calendar.DAY_OF_YEAR
import java.util.Date
import java.util.Locale

class ConfigureEventReportDate(
    private val creationType: EventCreationType = DEFAULT,
    private val resourceProvider: EventDetailResourcesProvider,
    private val repository: EventDetailsRepository,
    private val periodType: PeriodType? = null,
    private val periodUtils: DhisPeriodUtils,
    private val enrollmentId: String? = null,
    private val scheduleInterval: Int = 0,
) {

    operator fun invoke(selectedDate: Date? = null): Flow<EventDate> {
        return flowOf(
            EventDate(
                active = isActive(),
                label = getLabel(),
                dateValue = getDateValue(selectedDate),
                currentDate = getDate(selectedDate),
                minDate = getMinDate(selectedDate),
                maxDate = getMaxDate(),
                scheduleInterval = getScheduleInterval(),
                allowFutureDates = getAllowFutureDates(),
                periodType = periodType,
            ),
        )
    }

    private fun isActive(): Boolean {
        return !(creationType == SCHEDULE && getProgramStage()?.hideDueDate() == true)
    }

    private fun getLabel(): String {
        val programStage = getProgramStage()
        val event = repository.getEvent()

        return when (creationType) {
            SCHEDULE ->
                programStage?.displayDueDateLabel() ?: resourceProvider.provideDueDate()

            else -> {
                if (event == null) {
                    resourceProvider.provideNextEventDate(programStage?.displayEventLabel())
                } else {
                    programStage?.displayExecutionDateLabel() ?: resourceProvider.provideEventDate()
                }
            }
        }
    }

    private fun getDate(selectedDate: Date?) = when {
        selectedDate != null -> selectedDate
        repository.getEvent() != null -> repository.getEvent()?.eventDate()
        periodType != null || creationType == SCHEDULE -> getNextScheduleDate()
        else -> getCurrentDay()
    }

    private fun getDateValue(selectedDate: Date?) = getDate(selectedDate)?.let { date ->
        when {
            periodType != null ->
                periodUtils.getPeriodUIString(periodType, date, Locale.getDefault())

            else -> DateUtils.uiDateFormat().format(date)
        }
    }

    private fun getProgramStage(): ProgramStage? = repository.getProgramStage()

    private fun getDateBasedOnPeriodType(startDate: Date?): Date {
        val initialDate = startDate ?: DateUtils.getInstance().today
        val calendar = DateUtils.getInstance().calendar
        calendar.time = initialDate
        getProgramStage()?.hideDueDate()?.let { hideDueDate ->
            if (creationType == SCHEDULE && hideDueDate) {
                return if (periodType != null) {
                    calendar.time
                } else {
                    DateUtils.getInstance().getNextPeriod(
                        null,
                        calendar.time,
                        0,
                    )
                }
            }
        }
        return DateUtils.getInstance()
            .getNextPeriod(
                periodType,
                initialDate,
                if (creationType != SCHEDULE) 0 else 1,
            )
    }

    fun getNextScheduleDate(): Date {
        val scheduleDate = repository.getStageLastDate(enrollmentId)?.let {
            val lastStageDate = DateUtils.getInstance().getCalendarByDate(it)
            lastStageDate.add(DAY_OF_YEAR, getScheduleInterval())
            lastStageDate
        } ?: run {
            val enrollmentDate = with(repository) {
                when (getProgramStage()?.generatedByEnrollmentDate()) {
                    true -> getEnrollmentDate(enrollmentId)
                    else -> getEnrollmentIncidentDate(enrollmentId)
                        ?: getEnrollmentDate(enrollmentId)
                }
            }
            val date = DateUtils.getInstance().getCalendarByDate(enrollmentDate)
            val minDateFromStart = repository.getMinDaysFromStartByProgramStage()
            date.add(DAY_OF_YEAR, minDateFromStart)
            periodType?.let {
                return getDateBasedOnPeriodType(date.time)
            }
            return date.time
        }
        return DateUtils.getInstance().getNextPeriod(periodType, scheduleDate.time, if (periodType != null) 1 else 0)
    }

    private fun getCurrentDay() = DateUtils.getInstance().getStartOfDay(Date())

    private fun getMinDate(initialDate: Date?): Date? {
        repository.getProgram()?.let { program ->
            if (periodType == null) {
                if (program.expiryPeriodType() != null) {
                    val expiryDays = program.expiryDays() ?: 0
                    return DateUtils.getInstance().expDate(
                        initialDate,
                        expiryDays,
                        program.expiryPeriodType(),
                    )
                }
            } else {
                var minDate = DateUtils.getInstance().expDate(
                    initialDate,
                    program.expiryDays() ?: 0,
                    periodType,
                )
                val lastPeriodDate = DateUtils.getInstance().getNextPeriod(
                    periodType,
                    minDate,
                    -1,
                    true,
                )

                if (lastPeriodDate.after(
                        DateUtils.getInstance().getNextPeriod(
                            program.expiryPeriodType(),
                            minDate,
                            0,
                        ),
                    )
                ) {
                    minDate = DateUtils.getInstance()
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
                DEFAULT,
                -> Date(System.currentTimeMillis() - 1000)

                else -> null
            }
        } else {
            when (creationType) {
                ADDNEW,
                DEFAULT,
                -> DateUtils.getInstance().getStartOfDay(Date())

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
