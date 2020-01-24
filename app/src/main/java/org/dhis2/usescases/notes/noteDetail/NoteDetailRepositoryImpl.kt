package org.dhis2.usescases.notes.noteDetail

import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.note.Note
import org.hisp.dhis.android.core.note.NoteCreateProjection

class NoteDetailRepositoryImpl(private val d2: D2): NoteDetailRepository {

    override fun getNote(noteId: String): Single<Note> {
        return d2.noteModule().notes().uid(noteId).get()
    }

    override fun saveNote(type: NoteType, uid: String, message:String): Single<String> {
        return if (type == NoteType.ENROLLMENT){
            d2.noteModule().notes().add(
                NoteCreateProjection.builder()
                    .enrollment(
                        d2.enrollmentModule().enrollments()
                            .byProgram().eq("") //TODO: Needs program Uid
                            .byTrackedEntityInstance().eq(uid)
                            .byStatus().eq(EnrollmentStatus.ACTIVE)
                            .one().blockingGet().uid()
                    )
                    .value(message)
                    .build()
            )
        } else {
            Single.just("")
        }
    }
}