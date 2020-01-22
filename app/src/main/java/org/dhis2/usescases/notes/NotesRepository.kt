package org.dhis2.usescases.notes

import io.reactivex.Single
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.note.Note
import org.hisp.dhis.android.core.note.NoteCreateProjection
import java.util.Date

class NotesRepository(private val d2: D2, val programUid: String) {

    fun getEnrollmentNotes(teiUid: String) =
        d2.noteModule().notes()
            .byEnrollmentUid().eq(
                d2.enrollmentModule().enrollments()
                    .byProgram().eq(programUid)
                    .byTrackedEntityInstance().eq(teiUid)
                    .byStatus().eq(EnrollmentStatus.ACTIVE)
                    .one().blockingGet().uid()
            ).get()

    fun getEventNotes(eventUid: String) = Single.just(listOf<Note>(
        Note.builder()
            .uid("uid")
            .storedBy("The dude")
            .storedDate(DateUtils.databaseDateFormat().format(Date()))
            .value("Take it easy")
            .build()
    ))

    fun hasProgramWritePermission(): Boolean =
        d2.programModule().programs().uid(programUid).blockingGet().access().data().write()

    fun addEnrollmentNote(teiUid: String, message: String): Single<String> =
        d2.noteModule().notes().add(
            NoteCreateProjection.builder()
                .enrollment(
                    d2.enrollmentModule().enrollments()
                        .byProgram().eq(programUid)
                        .byTrackedEntityInstance().eq(teiUid)
                        .byStatus().eq(EnrollmentStatus.ACTIVE)
                        .one().blockingGet().uid()
                )
                .value(message)
                .build()
        )

    fun addEventNote(eventUid: String, message: String): Single<String> = Single.just("")

}