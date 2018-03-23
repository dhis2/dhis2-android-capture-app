package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.program.ProgramModel;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInfoSectionsPresenter implements EventInfoSectionsContract.Presenter {

    private EventInfoSectionsContract.View view;
    private final EventInfoSectionsContract.Interactor interactor;
    public ProgramModel program;

    EventInfoSectionsPresenter(EventInfoSectionsContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(@NonNull EventInfoSectionsContract.View view, @NonNull String eventId, @NonNull String programStageUid) {
        this.view = view;
        interactor.init(view, eventId, programStageUid);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }
}
