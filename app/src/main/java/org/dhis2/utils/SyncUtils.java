package org.dhis2.utils;

import java.util.List;

import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;
import timber.log.Timber;

public class SyncUtils {

    private SyncUtils() {
        // hide public constructor
    }

    public static boolean isSyncRunning(String syncTag) {
        List<WorkStatus> statuses = null;
        boolean running = false;
        try {
            statuses = WorkManager.getInstance().getStatusesByTag(syncTag).get();
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
