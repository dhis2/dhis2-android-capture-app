package dhis2.org.analytics.charts

import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue

interface BaseRepository {

    fun activeAndCompletedEventsWithData(
        enrollmentUid: String,
        stageUid: String,
        dataElementUid: String
    ): List<Pair<Event, TrackedEntityDataValue>>

    fun numericDataElements(stageUid: String): List<DataElement>
    fun repeatableStagesForEnrollment(enrollmentUid: String): List<ProgramStage>
    fun periodStep(periodType: PeriodType?): Long
}
