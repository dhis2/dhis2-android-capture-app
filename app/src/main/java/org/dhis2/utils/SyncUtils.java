package org.dhis2.utils;

import org.dhis2.data.tuples.Pair;

import java.util.List;

import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

public class SyncUtils {

    public static boolean isSyncRunning(String syncTag) {
        List<WorkStatus> statuses = null;
        boolean running = false;
        try {
            statuses = WorkManager.getInstance().getStatusesForUniqueWork(syncTag).get();
            for (WorkStatus workStatus : statuses) {
                if (workStatus.getState() == State.RUNNING)
                    running = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return running;
    }

    public static Pair<Boolean,Boolean> syncingState() {
        List<WorkStatus> metaStatus = null;
        List<WorkStatus> dataStatus = null;
        boolean metaRunning = false;
        boolean dataRunning = false;
        try {
            metaStatus = WorkManager.getInstance().getStatusesForUniqueWork(Constants.META).get();
            dataStatus = WorkManager.getInstance().getStatusesForUniqueWork(Constants.DATA).get();

            if (metaStatus.get(0).getState() == State.RUNNING) {
                metaRunning = true;
            } else if (dataStatus.get(0).getState() == State.RUNNING) {
                dataRunning = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Pair.create(metaRunning,dataRunning);
    }


    public static boolean isSyncRunning() {
        return isSyncRunning(Constants.META) || isSyncRunning(Constants.DATA);
    }

}
