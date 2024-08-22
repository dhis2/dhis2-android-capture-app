package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment

import io.reactivex.Flowable
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.model.AutoEnrollmentConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue

class AutoEnrollmentManagerImpl(private val d2:D2) : AutoEnrollmentManager {
    override fun getCurrentEventDataValues(eventUid: String?): Flowable<List<TrackedEntityDataValue>> {
        return d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid).get()
            .toFlowable()
    }

    override fun getCurrentEventTrackedEntityInstance(eventUid: String?): Flowable<String?>? {
        TODO("Not yet implemented")
    }

    override fun getAutoEnrollmentConfiguration(): Flowable<AutoEnrollmentConfig> {
        TODO("Not yet implemented")
    }

    override fun createEnrollments(
        programIds: List<String>,
        entity: String?,
        orgUnit: String?
    ): Flowable<String> {
        TODO("Not yet implemented")
    }
}