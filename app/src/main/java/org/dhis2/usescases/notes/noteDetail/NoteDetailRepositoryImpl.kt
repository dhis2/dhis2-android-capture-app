package org.dhis2.usescases.notes.noteDetail

import io.reactivex.Single
import org.dhis2.usescases.notes.NoteType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.note.Note
import org.hisp.dhis.android.core.note.NoteCreateProjection

class NoteDetailRepositoryImpl(
    private val d2: D2,
    private val programUid: String
) : NoteDetailRepository {

    override fun getNote(noteId: String): Single<Note> {
        return d2.noteModule().notes().uid(noteId).get()
    }

    override fun saveNote(type: NoteType, uid: String, message: String): Single<String> {
        return when (type) {
            NoteType.ENROLLMENT -> {
                d2.noteModule().notes().add(
                    NoteCreateProjection.builder()
                        .noteType(Note.NoteType.ENROLLMENT_NOTE)
                        .enrollment(
                            d2.enrollmentModule().enrollments()
                                .byProgram().eq(programUid)
                                .byTrackedEntityInstance().eq(uid)
                                .one().blockingGet().uid()
                        )
                        .value(message)
                        .build()
                )
            }
            NoteType.EVENT -> {
                d2.noteModule().notes().add(
                    NoteCreateProjection.builder()
                        .noteType(Note.NoteType.EVENT_NOTE)
                        .event(uid)
                        .value(message)
                        .build()
                )
            }
        }
    }
}
