package org.dhis2.usescases.notes

import io.reactivex.Single
import org.dhis2.Bindings.toDate
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.note.Note

class NotesRepository(private val d2: D2, val programUid: String) {

    fun getEnrollmentNotes(teiUid: String): Single<List<Note>> =
        d2.noteModule().notes()
            .byEnrollmentUid().eq(
                d2.enrollmentModule().enrollments()
                    .byProgram().eq(programUid)
                    .byTrackedEntityInstance().eq(teiUid)
                    .one().blockingGet().uid()
            ).get()
            .map { notes ->
                notes.sortedWith(Comparator { note1, note2 ->
                    note1.storedDate()?.toDate()?.compareTo(note2.storedDate()?.toDate()) ?: 0
                })
            }

    fun getEventNotes(eventUid: String): Single<List<Note>> {
        // TODO: Implement when sdk can get event notes
        return Single.just(listOf())
    }

    fun hasProgramWritePermission(): Boolean =
        d2.programModule().programs().uid(programUid).blockingGet().access().data().write()
}
