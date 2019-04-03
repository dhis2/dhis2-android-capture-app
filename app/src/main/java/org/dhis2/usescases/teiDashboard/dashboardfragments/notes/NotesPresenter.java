package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;

import io.reactivex.Flowable;

public interface NotesPresenter extends TeiDashboardContracts.Presenter {

    void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor);

    void subscribeToNotes(NotesFragment notesFragment);

    Boolean hasProgramWritePermission();
}
