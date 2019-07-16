package org.dhis2.data.service;

import android.content.Context;

import com.google.firebase.perf.metrics.AddTrace;

import org.dhis2.App;
import org.dhis2.usescases.main.program.SyncStatusDialog;

import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;
import io.reactivex.Single;
import timber.log.Timber;

import static org.dhis2.usescases.main.program.SyncStatusDialog.*;

public class SyncGranularRxWorker extends RxWorker {

    @Inject
    SyncPresenter presenter;

    public SyncGranularRxWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Single<Result> createWork() {
        Objects.requireNonNull(((App) getApplicationContext()).userComponent()).plus(new SyncGranularRxModule()).inject(this);
        String uid = getInputData().getString(UID);
        String conflictType = getInputData().getString(CONFLICTTYPE);
        /*switch (conflictType){
            case ConflictType.PROGRAM.name():
                break;
            case ConflictType.EVENT.name():
                break;
            default:
                break;
        }*/
        return Single.fromObservable(presenter.syncGranularEventObservable(uid)).map(d2Progress -> Result.success())
                .onErrorReturn(error -> Result.failure());

    }
}
