package com.dhis2.usescases.syncManager;

import com.dhis2.usescases.general.AbstractActivityContracts;

/**
 * Created by lmartin on 21/03/2018.
 */

public class SyncManagerContracts {

    interface View extends AbstractActivityContracts.View {
        void setLastDataSyncDate(String date);

        void setLastMetaDataSyncDate(String date);
    }

    public interface Presenter {

        void init(SyncManagerContracts.View view);

        void syncData(int seconds);

        void syncMeta(int seconds);
    }
}
