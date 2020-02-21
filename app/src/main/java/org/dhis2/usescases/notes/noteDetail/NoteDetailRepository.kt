package org.dhis2.usescases.notes.noteDetail

import io.reactivex.Single
import org.dhis2.usescases.notes.NoteType
import org.hisp.dhis.android.core.note.Note

interface NoteDetailRepository {
    fun getNote(noteId: String): Single<Note>
    fun saveNote(type: NoteType, uid: String, message:String): Single<String>
}