package org.dhis2.usescases.programEventDetail.usecase

import kotlinx.coroutines.withContext
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Calendar.DAY_OF_YEAR
import java.util.Date

class CreateEventUseCase(
    private val dispatcher: DispatcherProvider,
    private val d2: D2,
    private val dateUtils: DateUtils,
) {
    suspend operator fun invoke(
        programUid: String,
        orgUnitUid: String,
        programStageUid: String,
        enrollmentUid: String?,
    ): Result<String> = withContext(dispatcher.io()) {
        try {
            val stageLastDate = getStageLastDate(enrollmentUid, programStageUid)
            val programStage = getProgramStage(programStageUid)
            val eventUid = d2.eventModule().events().blockingAdd(
                EventCreateProjection.builder().apply {
                    enrollmentUid?.let { enrollment(enrollmentUid) }
                    program(programUid)
                    programStage(programStageUid)
                    organisationUnit(orgUnitUid)
                }.build(),
            )
            val eventDate = stageLastDate ?: when (programStage?.generatedByEnrollmentDate()) {
                true -> getEnrollmentDate(enrollmentUid)
                else -> getEnrollmentIncidentDate(enrollmentUid)
                    ?: getEnrollmentDate(enrollmentUid)
            }
            val eventRepository = d2.eventModule().events().uid(eventUid)
            val calendar = DateUtils.getInstance().getCalendarByDate(eventDate)
            if (stageLastDate == null) {
                val minDaysFromStart = getMinDaysFromStartByProgramStage(programStage)
                calendar.add(DAY_OF_YEAR, minDaysFromStart)
            } else {
                calendar.add(DAY_OF_YEAR, programStage?.standardInterval() ?: 0)
            }
            if (programStage?.periodType() == null) {
                eventRepository.setEventDate(calendar.time ?: dateUtils.today)
            } else {
                val nextAvailablePeriod = dateUtils.getNextPeriod(programStage.periodType(), calendar.time ?: dateUtils.today, 1)
                eventRepository.setEventDate(nextAvailablePeriod)
            }
            Result.success(eventUid)
        } catch (error: D2Error) {
            Result.failure(error)
        }
    }
    private fun getEnrollmentDate(uid: String?): Date? {
        val enrollment = d2.enrollmentModule().enrollments().byUid().eq(uid).blockingGet().first()
        return enrollment.enrollmentDate()
    }

    private fun getStageLastDate(enrollmentUid: String?, programStageUid: String?): Date? {
        val activeEvents =
            d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid).byProgramStageUid()
                .eq(programStageUid)
                .byDeleted().isFalse
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC).blockingGet()
        val scheduleEvents =
            d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid).byProgramStageUid()
                .eq(programStageUid)
                .byDeleted().isFalse
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC).blockingGet()

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

    private fun getMinDaysFromStartByProgramStage(programStage: ProgramStage?): Int {
        return programStage?.minDaysFromStart() ?: 0
    }

    private fun getEnrollmentIncidentDate(uid: String?): Date? {
        val enrollment = d2.enrollmentModule().enrollments().uid(uid).blockingGet()
        return enrollment?.incidentDate()
    }

    fun getProgramStage(programStageUid: String): ProgramStage? {
        return d2.programModule()
            .programStages()
            .uid(programStageUid)
            .blockingGet()
    }
}
