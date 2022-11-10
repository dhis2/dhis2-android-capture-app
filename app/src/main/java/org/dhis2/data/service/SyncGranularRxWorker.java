package org.dhis2.data.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import org.dhis2.commons.sync.ConflictType;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import timber.log.Timber;

import static org.dhis2.commons.Constants.ATTRIBUTE_OPTION_COMBO;
import static org.dhis2.commons.Constants.CATEGORY_OPTION_COMBO;
import static org.dhis2.commons.Constants.CONFLICT_TYPE;
import static org.dhis2.commons.Constants.ORG_UNIT;
import static org.dhis2.commons.Constants.PERIOD_ID;
import static org.dhis2.commons.Constants.UID;

public class SyncGranularRxWorker extends RxWorker {

    @Inject
    SyncPresenter presenter;

    public SyncGranularRxWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NotNull
    @Override
    public Single<Result> createWork() {
//        Objects.requireNonNull(((App) getApplicationContext()).userComponent()).plus(new SyncGranularRxModule()).inject(this);
        String uid = getInputData().getString(UID);
        ConflictType conflictType = ConflictType.valueOf(getInputData().getString(CONFLICT_TYPE));
        switch (conflictType) {
            case PROGRAM:
                return Single.fromObservable(presenter.syncGranularProgram(uid)).map(d2Progress -> {
                    if (!presenter.checkSyncProgramStatus(uid))
                        return Result.failure();
                    return Result.success();
                })
                        .onErrorReturn(error -> Result.failure());
            case TEI:
                return Single.fromObservable(presenter.syncGranularTEI(uid)).map(d2Progress -> checkTEISyncStatusResult(uid))
                        .onErrorReturn(error -> Result.failure());
            case EVENT:
                return Single.fromObservable(presenter.syncGranularEvent(uid))
                        .map(d2Progress -> checkEventSyncStatusResult(uid))
                        .doOnError(Timber::e)
                        .onErrorReturn(error -> Result.failure());
            case DATA_SET:
                return Single.fromObservable(presenter.syncGranularDataSet(uid)).map(d2Progress -> {
                    if (!presenter.checkSyncDataSetStatus(uid))
                        return Result.failure();

                    return Result.success();
                })
                        .onErrorReturn(error -> Result.failure());
            case DATA_VALUES:
                return Single.fromObservable(presenter.syncGranularDataValues(getInputData().getString(ORG_UNIT),
                        getInputData().getString(ATTRIBUTE_OPTION_COMBO), getInputData().getString(PERIOD_ID), getInputData().getStringArray(CATEGORY_OPTION_COMBO)))
                        .map(d2Progress -> {
                            if (!presenter.checkSyncDataValueStatus(getInputData().getString(ORG_UNIT),
                                    getInputData().getString(ATTRIBUTE_OPTION_COMBO), getInputData().getString(PERIOD_ID)))
                                return Result.failure();

                            return Result.success();
                        })
                        .onErrorReturn(error -> Result.failure());
            default:
                return Single.just(Result.failure());
        }

    }

    private Result checkEventSyncStatusResult(String uid) {
        switch (presenter.checkSyncEventStatus(uid)) {
            case ERROR:
                return Result.failure();
            case INCOMPLETE:
                Data data;
                data = new Data.Builder().putStringArray(
                        "conflict",
                        new String[]{"INCOMPLETE"}
                ).build();
                return Result.failure(data);
            default:
                return Result.success();
        }
    }

    private Result checkTEISyncStatusResult(String uid) {
        Data data;
        switch (presenter.checkSyncTEIStatus(uid)) {
            case ERROR:
                List<TrackerImportConflict> trackerImportConflicts =
                        presenter.messageTrackerImportConflict(uid);
                List<String> mergeDateConflicts = new ArrayList<>();
                for (TrackerImportConflict conflict : trackerImportConflicts) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(conflict.created().getTime());
                    String date = DateUtils.databaseDateFormat().format(calendar.getTime());
                    mergeDateConflicts.add(
                            date + "/" + conflict.displayDescription());
                }
                data = new Data.Builder().putStringArray("conflict",
                        mergeDateConflicts.toArray(new String[mergeDateConflicts.size()])).build();
                return Result.failure(data);
            case INCOMPLETE:
                data = new Data.Builder().putStringArray(
                        "conflict",
                        new String[]{"INCOMPLETE"}
                ).build();
                return Result.failure(data);
            default:
                return Result.success();
        }
    }
}
