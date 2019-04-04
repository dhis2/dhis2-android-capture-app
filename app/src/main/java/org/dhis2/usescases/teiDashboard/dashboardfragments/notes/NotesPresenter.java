package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import org.dhis2.data.tuples.Pair;

import io.reactivex.Flowable;

public interface NotesPresenter {

    void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor);

    void subscribeToNotes(NotesFragment notesFragment);

    Boolean hasProgramWritePermission();

    void onDettach();
}
