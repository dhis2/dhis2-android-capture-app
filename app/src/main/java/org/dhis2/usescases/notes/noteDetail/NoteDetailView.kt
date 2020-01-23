package org.dhis2.usescases.notes.noteDetail

interface NoteDetailView {
    fun showDiscardDialog()
    fun setNote(note: String)
    fun getNoteMessage(): String
}