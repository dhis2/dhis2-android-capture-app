package org.dhis2.usescases.searchTrackEntity

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import java.util.Date

class SearchNavigationConfiguration(val d2: D2) {
    private var openedTei: TrackedEntityInstance? = null
    private var openedEnrollment: Enrollment? = null

    fun openingTEI(teiUid: String) {
        openedTei = tei(teiUid)
    }

    fun openingEnrollmentForm(enrollmentUid: String) {
        openedEnrollment = enrollment(enrollmentUid)
    }

    fun refreshDataOnBackFromDashboard(): Boolean {
        val refresh = openedTei?.let { tei ->
            val previousLastUpdate = tei.lastUpdated() ?: Date()
            val newLastUpdate = tei(tei.uid())?.lastUpdated() ?: Date()
            return previousLastUpdate != newLastUpdate
        } ?: true

        openedTei = null

        return refresh
    }

    fun refreshDataOnBackFromEnrollment(): Boolean {
        val refresh = openedEnrollment?.let { enrollment ->
            enrollment(enrollment.uid()) != null
        } ?: false

        openedEnrollment = null

        return refresh
    }

    private fun tei(uid: String) = d2.trackedEntityModule().trackedEntityInstances()
        .uid(uid)
        .blockingGet()

    private fun enrollment(uid: String) = d2.enrollmentModule().enrollments().uid(uid).blockingGet()
}
