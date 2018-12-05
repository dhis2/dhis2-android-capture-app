package org.dhis2.usescases.sync;

import org.dhis2.usescases.general.AbstractActivityContracts;

import androidx.work.State;

public class SyncContracts {

    public interface View extends AbstractActivityContracts.View {

        void updateView(String data, State state);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view);
        void initMetaSync();
        void initDataSync();
        void initRVSync();
    }
}
