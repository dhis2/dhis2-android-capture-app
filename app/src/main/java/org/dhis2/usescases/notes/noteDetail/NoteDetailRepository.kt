package org.dhis2.usescases.notes.noteDetail

interface NoteDetailRepository {
    fun getNote(): String
    fun saveNote()
}