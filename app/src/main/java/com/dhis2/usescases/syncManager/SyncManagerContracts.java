package com.dhis2.usescases.syncManager;

import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.general.AbstractActivityContracts;

import io.reactivex.functions.Consumer;

/**
 * Created by lmartin on 21/03/2018.
 */

public class SyncManagerContracts {

    interface View extends AbstractActivityContracts.View {

        Consumer<Pair<Integer, Integer>> setSyncData();

        void wipeDatabase();

        void showTutorial();
    }

    public interface Presenter {

        void init(SyncManagerContracts.View view);

        void syncData(int seconds, String scheduleTag);

        void syncMeta(int seconds, String scheduleTag);

        void syncData();

        void syncMeta();

        void disponse();

        void resetSyncParameters();

        void onWipeData();

        void wipeDb();
    }
}
