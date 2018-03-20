package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import com.dhis2.usescases.general.AbstractActivityContracts;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInfoSectionsContract {

    public interface View extends AbstractActivityContracts.View {

    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(EventInfoSectionsContract.View mview, String eventId);

        void onBackClick();
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {
        void init(EventInfoSectionsContract.View view, String eventId);
    }
}
