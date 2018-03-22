package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import org.hisp.dhis.android.core.program.ProgramModel;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInfoSectionsPresenter implements EventInfoSectionsContract.Presenter {

    static private EventInfoSectionsContract.View view;
    private final EventInfoSectionsContract.Interactor interactor;
    public ProgramModel program;

    EventInfoSectionsPresenter(EventInfoSectionsContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(EventInfoSectionsContract.View view, String eventId) {
        interactor.init(view, eventId);
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
