package org.dhis2.usescases.syncManager;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;

import io.reactivex.functions.Consumer;

/**
 * Created by lmartin on 21/03/2018.
 */

public class SyncManagerContracts {

    interface View extends AbstractActivityContracts.View {
        void setLastDataSyncDate(String date);

        void setLastMetaDataSyncDate(String date);

        Consumer<Pair<Integer, Integer>> setSyncData();

        void wipeDatabase();
    }

    public interface Presenter {

        void init(SyncManagerContracts.View view);

        void syncData(int seconds, String scheduleTag);

        void syncMeta(int seconds, String scheduleTag);

        void disponse();

        void resetSyncParameters();

        void onWipeData();

        void wipeDb();
    }
}
