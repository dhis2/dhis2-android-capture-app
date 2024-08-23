package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment

import com.google.gson.Gson
import io.reactivex.Flowable
import org.dhis2.commons.date.DateUtils
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.model.AutoEnrollmentConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue

class AutoEnrollmentManagerImpl(private val d2: D2) : AutoEnrollmentManager {
    override fun getCurrentEventDataValues(eventUid: String): Flowable<List<TrackedEntityDataValue>> {
        return d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid).get()
            .toFlowable()
    }

    override fun getCurrentEventTrackedEntityInstance(eventUid: String): Flowable<String?>? {
        val currentEnrollment = d2.eventModule().events().uid(eventUid).blockingGet()?.enrollment()
        return d2.enrollmentModule().enrollments().uid(currentEnrollment).get()
            .map { ob -> ob.trackedEntityInstance() }
            .toFlowable()
    }

    override fun getAutoEnrollmentConfiguration(): Flowable<AutoEnrollmentConfig> {
        val ifExists = d2.dataStoreModule().dataStore().byNamespace().eq("workflow_redesign")
            .byKey().eq("configs").one().blockingGet()
        return if (ifExists != null) {
            d2.dataStoreModule().dataStore().byNamespace().eq("workflow_redesign")
                .byKey().eq("configs").one().get()
                .toFlowable()
                .map {
                    Gson().fromJson(
                        it.value(),
                        AutoEnrollmentConfig::class.java
                    )
                }
        } else Flowable.just(
            Gson().fromJson(
                AutoEnrollmentConfig.createDefaultAutoEnrollmentConfigObject(),
                AutoEnrollmentConfig::class.java
            )
        )
    }

    override fun createEnrollments(
        programIds: List<String>,
        entity: String?,
        orgUnit: String?
    ): Flowable<String> {


        return Flowable.fromIterable(programIds).map { id: String? ->
            val hasEnrollmentAccess = d2.enrollmentModule().enrollmentService()
                .blockingGetEnrollmentAccess(entity!!, id!!)
            val enrollementDoesnottExists = d2.enrollmentModule()
                .enrollments().byTrackedEntityInstance()
                .eq(entity).byProgram().eq(id).blockingIsEmpty()


            val orgUnitHasPrgoramAccess =
                d2.programModule().programs().byOrganisationUnitUid(orgUnit!!).byOrganisationUnitScope(
                    OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).uid(id).blockingGet()

            if ((hasEnrollmentAccess == EnrollmentAccess.WRITE_ACCESS)
                && enrollementDoesnottExists && orgUnitHasPrgoramAccess != null
            ) {
                val enrollment = d2.enrollmentModule().enrollments()
                    .blockingAdd(
                        EnrollmentCreateProjection.builder()
                            .trackedEntityInstance(entity)
                            .program(id)
                            .organisationUnit(orgUnit)
                            .build()
                    )
                d2.enrollmentModule().enrollments().uid(enrollment)
                    .setEnrollmentDate(DateUtils.getInstance().today)
                d2.enrollmentModule().enrollments().uid(enrollment)
                    .setIncidentDate(DateUtils.getInstance().today)
                d2.enrollmentModule().enrollments().uid(enrollment).setFollowUp(false)
                enrollment
            } else {
                return@map ""
            }
        }
    }



}