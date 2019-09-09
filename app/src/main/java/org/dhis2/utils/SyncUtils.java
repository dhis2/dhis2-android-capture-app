package org.dhis2.utils;

import android.content.Context;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

import timber.log.Timber;

public class SyncUtils {



    private static boolean isSyncRunning(String syncTag, Context context) {
        List<WorkInfo> statuses;
        boolean running = false;
        try {
            statuses = WorkManager.getInstance(context).getWorkInfosForUniqueWork(syncTag).get();
            for (WorkInfo workStatus : statuses) {
                if (workStatus.getState() == WorkInfo.State.RUNNING)
                    running = true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return running;
    }

    public static boolean isSyncRunning(Context context) {
        return isSyncRunning(Constants.META,context) || isSyncRunning(Constants.DATA,context);
    }

}
