package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.program.Program;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by Cristian on 01/03/2018.
 */

public class EventSummaryContract {

    public interface View extends AbstractActivityContracts.View {

        void setProgram(@NonNull Program program);

        void onEventSections(List<FormSectionViewModel> formSectionViewModels);

        @NonNull
        Consumer<List<FieldViewModel>> showFields(String sectionUid);

        void onStatusChanged(Event event);

        void setActionButton(Event eventModel);

        void messageOnComplete(String content, boolean canComplete);

        void checkAction();

        void accessDataWrite(Boolean canWrite);

        void fieldWithError(boolean b);
        void setHideSection(String sectionUid);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(@NonNull EventSummaryContract.View view, @NonNull String programId, @NonNull String eventId);

        void onBackClick();

        void getSectionCompletion(@Nullable String sectionUid);

        void onDoAction();

        void doOnComple();
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {

        void init(@NonNull EventSummaryContract.View view, @NonNull String programId, @NonNull String eventId);

        void getProgram(@NonNull String programUid);

        void getEventSections(@NonNull String programId);

        void getSectionCompletion(@Nullable String sectionUid);

        void onDoAction();

        void doOnComple();
    }
}
