package org.dhis2.usescases.enrollment

import io.reactivex.Single
import org.dhis2.bindings.profilePicturePath
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository

class EnrollmentFormRepositoryImpl(
    val d2: D2,
    enrollmentRepository: EnrollmentObjectRepository,
    programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
    teiRepository: TrackedEntityInstanceObjectRepository,
    private val enrollmentService: DhisEnrollmentUtils,
) : EnrollmentFormRepository {
    private var programUid: String =
        programRepository.blockingGet()?.uid() ?: throw NullPointerException()
    private var enrollmentUid: String =
        enrollmentRepository.blockingGet()?.uid() ?: throw NullPointerException()
    private val tei: TrackedEntityInstance =
        teiRepository.blockingGet() ?: throw NullPointerException()

    override fun generateEvents(): Single<Pair<String, String?>> =
        Single.fromCallable {
            enrollmentService.generateEnrollmentEvents(enrollmentUid, tei.uid())
        }

    override fun getProfilePicture() = tei.profilePicturePath(d2, programUid)

    override fun getProgramStageUidFromEvent(eventUi: String) =
        d2
            .eventModule()
            .events()
            .uid(eventUi)
            .blockingGet()
            ?.programStage()

    override fun hasWriteAccess(): Boolean =
        d2.enrollmentModule().enrollmentService().blockingGetEnrollmentAccess(
            tei.uid(),
            programUid,
        ) == EnrollmentAccess.WRITE_ACCESS
}
