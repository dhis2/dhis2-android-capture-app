package org.dhis2.data.dhislogic

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import javax.inject.Inject

class DhisEnrollmentUtils @Inject constructor(val d2: D2) {

    fun isEventEnrollmentOpen(event: Event): Boolean {
        return if (event.enrollment() != null) {
            val enrollment = d2.enrollmentModule().enrollments()
                .uid(event.enrollment())
                .blockingGet()
            enrollment == null || enrollment.status() == EnrollmentStatus.ACTIVE
        } else {
            true
        }
    }

    fun isTrackedEntityAttributeValueUnique(uid: String, value: String?, teiUid: String): Boolean {
        if (value == null) {
            return true
        }

        val localUid =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()!!
        val isUnique = localUid.unique() ?: false
        val orgUnitScope = localUid.orgUnitScope() ?: false

        if (!isUnique) {
            return true
        }

        return if (!orgUnitScope) {
            val hasValue = getTrackedEntityAttributeValues(uid, value, teiUid).isNotEmpty()
            !hasValue
        } else {
            val enrollingOrgUnit = getOrgUnit(teiUid)
            val hasValue = getTrackedEntityAttributeValues(uid, value, teiUid)
                .map {
                    getOrgUnit(it.trackedEntityInstance()!!)
                }
                .all { it != enrollingOrgUnit }
            hasValue
        }
    }

    fun generateEnrollmentEvents(enrollmentUid: String): Pair<String, String?> {
        return EnrollmentEventGenerator(
            EnrollmentEventGeneratorRepositoryImpl(d2),
        ).generateEnrollmentEvents(enrollmentUid)
    }

    fun getOrgUnit(teiUid: String): String {
        return d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
            ?.organisationUnit()
            ?: throw NoSuchElementException("Organisation unit not found for trackedEntity $teiUid")
    }

    private fun getTrackedEntityAttributeValues(
        uid: String,
        value: String,
        teiUid: String,
    ): List<TrackedEntityAttributeValue> {
        return d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityAttribute().eq(uid)
            .byTrackedEntityInstance().neq(teiUid)
            .byValue().eq(value).blockingGet()
    }
}
