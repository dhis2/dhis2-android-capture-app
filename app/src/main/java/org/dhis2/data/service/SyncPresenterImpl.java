package org.dhis2.data.service;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;

import io.reactivex.Completable;
import timber.log.Timber;

final class SyncPresenterImpl implements SyncPresenter {

    @NonNull
    private final D2 d2;

    SyncPresenterImpl(@NonNull D2 d2) {
        this.d2 = d2;
    }

    @Override
    public void syncAndDownloadEvents(Context context) throws Exception {
        d2.eventModule().events.upload().call();
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        int eventLimit = prefs.getInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT);
        boolean limitByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        boolean limitByProgram = prefs.getBoolean(Constants.LIMIT_BY_PROGRAM, false);
        d2.eventModule().downloadSingleEvents(eventLimit, limitByOU, limitByProgram).call();
    }

    @Override
    public void syncAndDownloadTeis(Context context) throws Exception {
        d2.trackedEntityModule().trackedEntityInstances.upload().call();
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        int teiLimit = prefs.getInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT);
        boolean limitByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        boolean limitByProgram = prefs.getBoolean(Constants.LIMIT_BY_PROGRAM, false);
        Completable.fromObservable(d2.trackedEntityModule()
                .downloadTrackedEntityInstances(teiLimit, limitByOU, limitByProgram)
                .doOnNext(data -> Timber.d(data.percentage() + "% " + data.doneCalls().size() + "/" + data.totalCalls())))
                .blockingAwait();
    }

    @Override
    public void syncAndDownloadDataValues() throws Exception {
        d2.dataValueModule().dataValues.upload().call();
        d2.dataSetModule().dataSetCompleteRegistrations.upload().call();
        Completable.fromObservable(d2.aggregatedModule().data().download()).blockingAwait();
    }

    @Override
    public void syncMetadata(Context context) {
        Completable.fromObservable(d2.syncMetaData()).blockingAwait();
    }

    @Override
    public void syncReservedValues() {
        d2.trackedEntityModule().reservedValueManager.syncReservedValues(null, null, 100);
    }
}
