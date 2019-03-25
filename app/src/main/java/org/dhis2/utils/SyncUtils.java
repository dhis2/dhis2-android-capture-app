package org.dhis2.utils;

import java.util.List;

import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;
import timber.log.Timber;

public class SyncUtils {


    public enum SyncState {
        TO_START,
        METADATA,
        METADATA_FINISHED,
        DATA,
        DATA_FINISHED
    }

    public static boolean isSyncRunning(String syncTag) {
        List<WorkStatus> statuses;
        boolean running = false;
        try {
            statuses = WorkManager.getInstance().getStatusesForUniqueWork(syncTag).get();
            for (WorkStatus workStatus : statuses) {
                if (workStatus.getState() == State.RUNNING)
                    running = true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return running;
    }

    public static boolean isSyncRunning() {
        return isSyncRunning(Constants.META) || isSyncRunning(Constants.DATA);
    }

}
