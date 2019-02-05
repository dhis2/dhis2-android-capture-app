package org.dhis2.utils;

import java.util.List;

import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

public class SyncUtils {

    public static boolean isSyncRunning(String syncTag){
        List<WorkStatus> statuses = null;
        boolean running = false;
        try {
            statuses = WorkManager.getInstance().getStatusesByTag(syncTag).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (WorkStatus workStatus : statuses) {
            if(workStatus.getState() == State.RUNNING)
                running=true;
        }
        return running;
    }
    public static boolean isSyncRunning(){
        return isSyncRunning(Constants.META) || isSyncRunning(Constants.DATA);
    }

}
