package dhis2.org.analytics.charts

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import java.util.Date

open class BaseChartRepositoryImpl(private val d2: D2) : BaseRepository {

    override fun periodStep(periodType: PeriodType?): Long {
        val currentDate = Date()
        val initialPeriodDate =
            d2.periodModule().periodHelper().blockingGetPeriodForPeriodTypeAndDate(
                periodType ?: PeriodType.Daily,
                currentDate,
                -1
            ).startDate()?.time ?: 0L
        val currentPeriodDate =
            d2.periodModule().periodHelper().blockingGetPeriodForPeriodTypeAndDate(
                periodType ?: PeriodType.Daily,
                currentDate,
                0
            ).startDate()?.time ?: 0L
        return currentPeriodDate - initialPeriodDate
    }

    override fun repeatableStagesForEnrollment(enrollmentUid: String): List<ProgramStage> {
        val programUid = d2.enrollmentModule().enrollments().uid(enrollmentUid)
            .blockingGet()
            .program()!!
        return d2.programModule().programStages()
            .byProgramUid().eq(programUid)
            .byRepeatable().isTrue
            .blockingGet()
    }

    override fun numericDataElements(stageUid: String): List<DataElement> {
        return d2.programModule().programStageDataElements()
            .byProgramStage().eq(stageUid)
            .blockingGet().filter {
                d2.dataElementModule().dataElements().uid(it.dataElement()?.uid())
                    .blockingGet().valueType()?.isNumeric ?: false
            }.map {
                d2.dataElementModule().dataElements().uid(
                    it.dataElement()?.uid()
                ).blockingGet()
            }
    }

    override fun activeAndCompletedEventsWithData(
        enrollmentUid: String,
        stageUid: String,
        dataElementUid: String
    ): List<Pair<Event,TrackedEntityDataValue>> {
        return d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().eq(stageUid)
            .byDeleted().isFalse
            .orderByEventDate(RepositoryScope.OrderByDirection.ASC)
            .byStatus().`in`(EventStatus.ACTIVE, EventStatus.COMPLETED)
            .blockingGet().filter {
                hasData(it.uid(), dataElementUid)
            }.map {
                Pair(
                    it,
                    geTrackedEntityDataValue(it.uid(),dataElementUid)
                )
            }
    }

    private fun hasData(eventUid: String, dataElementUid: String): Boolean {
        return d2.trackedEntityModule().trackedEntityDataValues()
            .value(eventUid, dataElementUid).blockingExists()
    }

    private fun geTrackedEntityDataValue(eventUid: String, dataElementUid: String): TrackedEntityDataValue {
        return d2.trackedEntityModule().trackedEntityDataValues()
            .value(eventUid, dataElementUid).blockingGet()
    }
}