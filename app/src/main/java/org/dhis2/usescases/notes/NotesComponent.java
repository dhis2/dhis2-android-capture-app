package org.dhis2.usescases.notes;

import org.dhis2.commons.di.dagger.PerFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = NotesModule.class)
public interface NotesComponent {

    void inject(NotesFragment notesFragment);

}
