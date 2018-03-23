package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;

import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.program.ProgramStageSectionModel;

import java.util.List;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInfoSectionsContract {

    public interface View extends AbstractActivityContracts.View {
        void renderError(String message);

        void setProgramStageSections(List<ProgramStageSectionModel> programStageSectionModelList);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(@NonNull EventInfoSectionsContract.View view, @NonNull String eventId, @NonNull String programStageUid);

        void onBackClick();
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {
        void init(@NonNull EventInfoSectionsContract.View view, @NonNull String eventId, @NonNull String programStageUid);
    }
}
