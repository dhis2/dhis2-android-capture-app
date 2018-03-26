package com.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import com.dhis2.data.forms.FormSectionViewModel;
import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventSummaryContract {

    public interface View extends AbstractActivityContracts.View {

        void setProgram(ProgramModel program);

        void onEventSections(List<FormSectionViewModel> formSectionViewModels);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(EventSummaryContract.View view, String programId, String eventId);

        void onBackClick();
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {

        void init(EventSummaryContract.View view, String programId, String eventId);

        void getProgram(String programUid);

        void getEventSections(String programId);
    }
}
