package org.dhis2.usescases.notes;

import java.util.List;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.note.Note;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class NotesContracts {

    public interface View extends AbstractActivityContracts.View {

        Consumer<List<Note>> swapNotes();
    }
}
