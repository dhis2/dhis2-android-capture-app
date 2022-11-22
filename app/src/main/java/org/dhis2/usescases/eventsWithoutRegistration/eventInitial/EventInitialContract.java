package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import androidx.annotation.NonNull;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

public class EventInitialContract {

    public interface View extends AbstractActivityContracts.View {

        void setProgram(@NonNull Program program);

        void renderError(String message);

        void onEventCreated(String eventUid);

        void onEventUpdated(String eventUid);

        void setProgramStage(ProgramStage programStage);

        void updatePercentage(float primaryValue);

        void showProgramStageSelection();

        void setAccessDataWrite(Boolean canWrite);

        void showQR();

        void showEventWasDeleted();
    }
}
