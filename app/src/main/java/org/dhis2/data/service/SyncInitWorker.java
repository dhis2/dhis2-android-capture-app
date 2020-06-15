package org.dhis2.data.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.perf.metrics.AddTrace;

import org.dhis2.App;

import javax.inject.Inject;

/**
 * QUADRAM. Created by ppajuelo on 23/10/2018.
 */

public class SyncInitWorker extends Worker {

    public static final String INIT_META = "INIT_META";
    public static final String INIT_DATA = "INIT_DATA";

    @Inject
    SyncPresenter presenter;

    public SyncInitWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    @AddTrace(name = "MetadataSyncTrace")
    public Result doWork() {

        if (((App) getApplicationContext()).userComponent() != null) {

            ((App) getApplicationContext()).userComponent().plus(new SyncInitWorkerModule()).inject(this);

            if (getInputData().getBoolean(INIT_META, false))
                presenter.startPeriodicMetaWork();
            if (getInputData().getBoolean(INIT_DATA, false))
                presenter.startPeriodicDataWork();

            return Result.success();
        } else {
            return Result.failure();
        }
    }

}
