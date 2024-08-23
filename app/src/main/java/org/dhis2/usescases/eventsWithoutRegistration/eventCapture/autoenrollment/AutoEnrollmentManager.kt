package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment

import io.reactivex.Flowable
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.model.AutoEnrollmentConfig
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue

interface AutoEnrollmentManager {

    fun getCurrentEventDataValues(eventUid: String): Flowable<List<TrackedEntityDataValue>>

    fun getCurrentEventTrackedEntityInstance(eventUid: String): Flowable<String?>?

    fun getAutoEnrollmentConfiguration(): Flowable<AutoEnrollmentConfig>


    fun createEnrollments(
        programIds: List<String>,
        entity: String?,
        orgUnit: String?
    ): Flowable<String>


}