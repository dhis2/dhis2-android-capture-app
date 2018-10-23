package org.dhis2.data.service;

import android.content.Context;
import android.support.annotation.NonNull;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * QUADRAM. Created by ppajuelo on 23/10/2018.
 */

public class SyncDataWorker extends Worker {

    public SyncDataWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return Result.SUCCESS;
    }
}
