package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.enrollment.note.Note;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class NotesContracts {

    public interface View extends AbstractActivityContracts.View {

        Consumer<List<Note>> swapNotes();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init(View view);

        void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor);

        void subscribeToNotes();

        boolean hasProgramWritePermission();
    }

}
