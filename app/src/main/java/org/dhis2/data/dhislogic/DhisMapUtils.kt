package org.dhis2.data.dhislogic

import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage

class DhisMapUtils @Inject constructor(val d2: D2) {

    fun getCoordinateDataElementInfo(eventUidList: List<String>): List<CoordinateDataElementInfo> {
        return d2.trackedEntityModule().trackedEntityDataValues()
            .byEvent().`in`(eventUidList)
            .blockingGet().filter { trackedEntityDataValue ->
                d2.dataElementModule().dataElements()
                    .uid(trackedEntityDataValue.dataElement()).blockingGet()
                    .valueType() == ValueType.COORDINATE
            }.map {
                val event = d2.eventModule().events().uid(it.event()).blockingGet()
                val stage = d2.programModule().programStages()
                    .uid(event.programStage()).blockingGet()
                val geometry = Geometry.builder()
                    .coordinates(it.value())
                    .type(FeatureType.POINT)
                    .build()
                val de = d2.dataElementModule().dataElements()
                    .uid(it.dataElement()).blockingGet()
                val enrollment = event.enrollment()?.let { enrollmentUid ->
                    d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
                }

                CoordinateDataElementInfo(
                    event,
                    stage,
                    de,
                    enrollment,
                    geometry
                )
            }
    }
}

data class CoordinateDataElementInfo(
    val event: Event,
    val stage: ProgramStage,
    val dataElement: DataElement,
    val enrollment: Enrollment?,
    val geometry: Geometry
)
