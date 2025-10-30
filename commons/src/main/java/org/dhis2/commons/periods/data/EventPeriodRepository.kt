package org.dhis2.commons.periods.data

import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.eventsBy
import org.dhis2.commons.bindings.program
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Date

class EventPeriodRepository(
    private val d2: D2,
) {
    fun getEventPeriodMinDate(
        programStage: ProgramStage,
        isScheduling: Boolean,
        eventEnrollmentUid: String?,
    ): Date {
        val periodType = programStage.periodType() ?: PeriodType.Daily

        val program = programStage.program()?.let { d2.program(it.uid()) }

        val expiryDays = program?.expiryDays()

        val currentDate =
            if (!isScheduling && eventEnrollmentUid != null) {
                val enrollment = d2.enrollment(eventEnrollmentUid)
                if (programStage.generatedByEnrollmentDate() == true) {
                    enrollment?.enrollmentDate()
                } else {
                    enrollment?.incidentDate() ?: enrollment?.enrollmentDate()
                }
            } else {
                generatePeriod(periodType, offset = 1).startDate()
            } ?: Date()

        val currentPeriod = generatePeriod(periodType, currentDate)
        val previousPeriodLastDay =
            generatePeriod(PeriodType.Daily, currentPeriod.startDate()!!, expiryDays ?: 0)
                .startDate()

        return if (currentDate.after(previousPeriodLastDay) or (currentDate == previousPeriodLastDay)) {
            currentPeriod.startDate()
        } else {
            generatePeriod(periodType, currentDate, offset = -1).startDate()
        } ?: Date()
    }

    fun getEventPeriodMaxDate(
        programStage: ProgramStage,
        isScheduling: Boolean,
        eventEnrollmentUid: String?,
    ): Date? {
        if (isScheduling) return null

        val periodType = programStage.periodType() ?: PeriodType.Daily

        val program = programStage.program()?.let { d2.program(it.uid()) }

        val expiryDays = program?.expiryDays()

        val currentDate =
            if (expiryDays == null) {
                val enrollment = eventEnrollmentUid?.let { d2.enrollment(it) }
                if (programStage.generatedByEnrollmentDate() == true) {
                    enrollment?.enrollmentDate()
                } else {
                    enrollment?.incidentDate() ?: enrollment?.enrollmentDate()
                }
            } else {
                Date()
            } ?: Date()

        val currentPeriod = generatePeriod(periodType, currentDate)

        return currentPeriod.startDate()
    }

    fun getEventUnavailableDates(
        programStageUid: String,
        enrollmentUid: String?,
        currentEventUid: String?,
    ): List<Date> {
        val enrollment = enrollmentUid?.let { d2.enrollment(it) }
        return d2.eventsBy(enrollmentUid = enrollment?.uid()).mapNotNull {
            if (it.programStage() == programStageUid &&
                (currentEventUid == null || it.uid() != currentEventUid) &&
                it.status() != EventStatus.SKIPPED &&
                it.deleted() == false
            ) {
                it.eventDate() ?: it.dueDate()
            } else {
                null
            }
        }
    }

    private fun generatePeriod(
        periodType: PeriodType,
        date: Date = Date(),
        offset: Int = 0,
    ) = d2
        .periodModule()
        .periodHelper()
        .blockingGetPeriodForPeriodTypeAndDate(periodType, date, offset)

    fun getPeriodSource(
        periodLabelProvider: PeriodLabelProvider,
        selectedDate: Date?,
        periodType: PeriodType,
        initialDate: Date,
        maxDate: Date?,
    ): PeriodSource =
        PeriodSource(
            d2 = d2,
            periodLabelProvider = periodLabelProvider,
            periodType = periodType,
            initialDate = initialDate,
            maxDate = maxDate,
            selectedDate = selectedDate,
        )
}
