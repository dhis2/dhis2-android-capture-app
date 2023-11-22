package org.dhis2.maps.utils

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import javax.inject.Inject

class DhisMapUtils @Inject constructor(val d2: D2) {

    fun getCoordinateDataElementInfo(eventUidList: List<String>): List<CoordinateDataElementInfo> {
        return d2.trackedEntityModule().trackedEntityDataValues()
            .byEvent().`in`(eventUidList)
            .blockingGet().filter { trackedEntityDataValue ->
                dataElementHasCoordinateValue(trackedEntityDataValue)
            }.mapNotNull {
                val event = d2.eventModule().events().uid(it.event()).blockingGet()
                val stage = d2.programModule().programStages()
                    .uid(event?.programStage()).blockingGet()
                val geometry = Geometry.builder()
                    .coordinates(it.value())
                    .type(FeatureType.POINT)
                    .build()
                val de = d2.dataElementModule().dataElements()
                    .uid(it.dataElement()).blockingGet()
                val enrollment = event?.enrollment()?.let { enrollmentUid ->
                    d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
                }

                if (event != null && stage != null && de != null) {
                    CoordinateDataElementInfo(
                        event,
                        stage,
                        de,
                        enrollment,
                        geometry,
                    )
                } else {
                    null
                }
            }
    }

    private fun dataElementHasCoordinateValue(
        trackedEntityDataValue: TrackedEntityDataValue,
    ): Boolean {
        val isCoordinateValueType = d2.dataElementModule().dataElements()
            .uid(trackedEntityDataValue.dataElement()).blockingGet()
            ?.valueType() == ValueType.COORDINATE
        val hasValue = trackedEntityDataValue.value() != null
        return isCoordinateValueType && hasValue
    }

    fun getCoordinateAttributeInfo(teiUidList: List<String>): List<CoordinateAttributeInfo> {
        return d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityInstance().`in`(teiUidList)
            .blockingGet().filter { trackedEntityAttributeValue ->
                attributeHasCoordinateValue(trackedEntityAttributeValue)
            }.mapNotNull {
                val tei = d2.trackedEntityModule().trackedEntityInstances()
                    .uid(it.trackedEntityInstance()).blockingGet()
                val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                    .uid(it.trackedEntityAttribute()).blockingGet()
                val geometry = Geometry.builder()
                    .coordinates(it.value())
                    .type(FeatureType.POINT)
                    .build()
                if (tei != null && attribute != null) {
                    CoordinateAttributeInfo(
                        tei,
                        attribute,
                        geometry,
                    )
                } else {
                    null
                }
            }
    }

    private fun attributeHasCoordinateValue(
        trackedEntityAttributeValue: TrackedEntityAttributeValue,
    ): Boolean {
        val isCoordinateValueType = d2.trackedEntityModule().trackedEntityAttributes()
            .uid(trackedEntityAttributeValue.trackedEntityAttribute()).blockingGet()
            ?.valueType() == ValueType.COORDINATE
        val hasValue = trackedEntityAttributeValue.value() != null
        return isCoordinateValueType && hasValue
    }
}

sealed class CoordinateFieldInfo

data class CoordinateDataElementInfo(
    val event: Event,
    val stage: ProgramStage,
    val dataElement: DataElement,
    val enrollment: Enrollment?,
    val geometry: Geometry,
) : CoordinateFieldInfo()

data class CoordinateAttributeInfo(
    val tei: TrackedEntityInstance,
    val attribute: TrackedEntityAttribute,
    val geometry: Geometry,
) : CoordinateFieldInfo()
