package org.dhis2.utils;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

import timber.log.Timber;

public class SyncUtils {


    private static boolean isSyncRunning(String syncTag) {
        List<WorkInfo> statuses;
        boolean running = false;
        try {
            statuses = WorkManager.getInstance().getWorkInfosForUniqueWork(syncTag).get();
            for (WorkInfo workStatus : statuses) {
                if (workStatus.getState() == WorkInfo.State.RUNNING)
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
