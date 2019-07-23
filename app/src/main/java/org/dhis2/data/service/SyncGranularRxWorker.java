package org.dhis2.data.service;

import android.content.Context;

import org.dhis2.App;

import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;
import io.reactivex.Single;

import static org.dhis2.usescases.main.program.SyncStatusDialog.*;
import static org.dhis2.utils.Constants.*;

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
        ConflictType conflictType = ConflictType.valueOf(getInputData().getString(CONFLICT_TYPE));
        switch (conflictType){
            case PROGRAM:
                return Single.fromObservable(presenter.syncGranularProgram(uid)).map(d2Progress -> Result.success())
                        .onErrorReturn(error -> Result.failure());
            case TEI:
                return Single.fromObservable(presenter.syncGranularTEI(uid)).map(d2Progress -> Result.success())
                        .onErrorReturn(error -> Result.failure());
            case EVENT:
                return Single.fromObservable(presenter.syncGranularEvent(uid)).map(d2Progress -> Result.success())
                        .onErrorReturn(error -> Result.failure());
            case DATA_SET:
                return Single.fromObservable(presenter.syncGranularDataSet(uid)).map(d2Progress -> Result.success())
                        .onErrorReturn(error -> Result.failure());
            case DATA_VALUES:
                return Single.fromObservable(presenter.syncGranularDataValues(getInputData().getString(ORG_UNIT),
                        getInputData().getString(ATTRIBUTE_OPTION_COMBO), getInputData().getString(PERIOD_ID)))
                        .map(d2Progress -> Result.success())
                        .onErrorReturn(error -> Result.failure());
            default:
                return Single.just(Result.failure());
        }

    }
}
