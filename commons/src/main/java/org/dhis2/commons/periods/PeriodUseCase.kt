package org.dhis2.commons.periods

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.eventsBy
import org.dhis2.commons.bindings.program
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Date

class PeriodUseCase(private val d2: D2) {
    private val periodHelper = d2.periodModule().periodHelper()
    fun fetchPeriods(
        periodType: PeriodType,
        selectedDate: Date?,
        initialDate: Date,
        maxDate: Date?,
    ): Flow<PagingData<Period>> = Pager(
        config = PagingConfig(pageSize = 10, maxSize = 100),
        pagingSourceFactory = {
            PeriodSource(
                periodHelper = periodHelper,
                periodType = periodType,
                initialDate = initialDate,
                maxDate = maxDate,
                selectedDate = selectedDate,
            )
        },
    ).flow

    fun getEventPeriodMinDate(
        programStage: ProgramStage,
        isScheduling: Boolean,
        eventEnrollmentUid: String?,
    ): Date {
        val periodType = programStage.periodType() ?: PeriodType.Daily

        val program = programStage.program()?.let { d2.program(it.uid()) }

        val expiryDays = program?.expiryDays()

        val currentDate = if (!isScheduling && expiryDays == null) {
            val enrollment = eventEnrollmentUid?.let { d2.enrollment(it) }
            if (programStage.generatedByEnrollmentDate() == true) {
                enrollment?.enrollmentDate()
            } else {
                enrollment?.incidentDate() ?: enrollment?.enrollmentDate()
            }
        } else {
            d2.generatePeriod(periodType, offset = 1).startDate()
        } ?: Date()

        val currentPeriod = d2.generatePeriod(periodType)
        val previousPeriodLastDay =
            d2.generatePeriod(PeriodType.Daily, currentPeriod.startDate()!!, expiryDays ?: 0)
                .startDate()

        return if (currentDate.after(previousPeriodLastDay)) {
            currentPeriod.startDate()
        } else {
            d2.generatePeriod(periodType, offset = -1).startDate()
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

        val currentDate = if (expiryDays == null) {
            val enrollment = eventEnrollmentUid?.let { d2.enrollment(it) }
            if (programStage.generatedByEnrollmentDate() == true) {
                enrollment?.enrollmentDate()
            } else {
                enrollment?.incidentDate() ?: enrollment?.enrollmentDate()
            }
        } else {
            Date()
        } ?: Date()

        val currentPeriod = d2.generatePeriod(periodType, currentDate)

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
                it.status() != EventStatus.SKIPPED
            ) {
                it.eventDate()
            } else {
                null
            }
        }
    }
}

private fun D2.generatePeriod(
    periodType: PeriodType,
    date: Date = Date(),
    offset: Int = 0,
) = periodModule().periodHelper()
    .blockingGetPeriodForPeriodTypeAndDate(periodType, date, offset)
