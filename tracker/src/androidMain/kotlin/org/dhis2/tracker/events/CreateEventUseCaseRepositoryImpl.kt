package org.dhis2.tracker.events

import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Date

class CreateEventUseCaseRepositoryImpl(
    private val d2: D2,
    private val dateUtils: DateUtils,
) : CreateEventUseCaseRepository {
    override suspend fun createEvent(
        enrollmentUid: String?,
        programUid: String,
        programStageUid: String?,
        orgUnitUid: String,
    ): Result<String> =
        try {
            val stageLastDate = getStageLastDate(enrollmentUid, programStageUid)
            val programStage = getProgramStage(programStageUid)
            val eventUid =
                d2.eventModule().events().blockingAdd(
                    EventCreateProjection
                        .builder()
                        .apply {
                            enrollmentUid?.let { enrollment(enrollmentUid) }
                            program(programUid)
                            programStage(programStageUid)
                            organisationUnit(orgUnitUid)
                        }.build(),
                )
            setEventDate(eventUid, programStage, stageLastDate)
            Result.success(eventUid)
        } catch (error: D2Error) {
            Result.failure(error)
        }

    private fun setEventDate(
        eventUid: String,
        programStage: ProgramStage?,
        stageLastDate: Date?,
    ) {
        val currentDate = dateUtils.getStartOfDay(Date())
        val eventDate =
            if (stageLastDate != null && programStage?.periodType() != null) {
                val currentPeriod = dateUtils.getNextPeriod(programStage.periodType(), currentDate, 0)
                val lastEventDatePeriod =
                    dateUtils.getNextPeriod(programStage.periodType(), stageLastDate, 0)
                if (currentPeriod == lastEventDatePeriod || lastEventDatePeriod.after(currentPeriod)) {
                    dateUtils.getNextPeriod(programStage.periodType(), currentDate, 1)
                } else {
                    dateUtils.getNextPeriod(programStage.periodType(), currentDate, 0)
                }
            } else {
                currentDate
            }

        if (eventDate.before(currentDate) || eventDate == currentDate) {
            val eventRepository = d2.eventModule().events().uid(eventUid)
            eventRepository.setEventDate(eventDate)
        }
    }

    private fun getStageLastDate(
        enrollmentUid: String?,
        programStageUid: String?,
    ): Date? {
        val activeEvents =
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq(enrollmentUid)
                .byProgramStageUid()
                .eq(programStageUid)
                .byDeleted()
                .isFalse
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()
        val scheduleEvents =
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq(enrollmentUid)
                .byProgramStageUid()
                .eq(programStageUid)
                .byDeleted()
                .isFalse
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()

        var activeDate: Date? = null
        var scheduleDate: Date? = null
        if (activeEvents.isNotEmpty()) {
            activeDate = activeEvents[0].eventDate()
        }
        if (scheduleEvents.isNotEmpty()) scheduleDate = scheduleEvents[0].dueDate()

        return when {
            scheduleDate == null -> activeDate
            activeDate == null -> scheduleDate
            activeDate.before(scheduleDate) -> scheduleDate
            else -> activeDate
        }
    }

    private fun getProgramStage(programStageUid: String?): ProgramStage? =
        d2
            .programModule()
            .programStages()
            .uid(programStageUid)
            .blockingGet()
}
