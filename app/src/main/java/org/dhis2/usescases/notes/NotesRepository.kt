package org.dhis2.usescases.notes

import io.reactivex.Single
import org.dhis2.bindings.toDate
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.note.Note
import org.hisp.dhis.android.core.note.NoteCollectionRepository

class NotesRepository(
    private val d2: D2,
    val programUid: String,
    private val enrollmentUid: String?,
) {
    fun getEnrollmentNotes(): Single<List<Note>> =
        d2
            .noteModule()
            .notes()
            .byEnrollmentUid()
            .eq(enrollmentUid)
            .getSortedByStoredDate()

    fun getEventNotes(eventUid: String): Single<List<Note>> =
        d2
            .noteModule()
            .notes()
            .byEventUid()
            .eq(eventUid)
            .getSortedByStoredDate()

    fun hasProgramWritePermission(): Boolean =
        d2
            .programModule()
            .programs()
            .uid(programUid)
            .blockingGet()
            ?.access()
            ?.data()
            ?.write() == true

    private fun NoteCollectionRepository.getSortedByStoredDate(): Single<List<Note>> =
        rxGet().map { notes -> notes.sortedBy { note -> note.storedDate()?.toDate() } }
}
