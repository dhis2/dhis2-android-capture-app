package org.dhis2.usescases.notes.noteDetail

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity

@PerActivity
@Subcomponent(modules = [NoteDetailModule::class])
interface NoteDetailComponent {
    fun inject(noteDetailActivity: NoteDetailActivity)
}
