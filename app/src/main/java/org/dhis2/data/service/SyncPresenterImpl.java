package org.dhis2.data.service;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.dhis2.utils.ConstantsKt;
import org.hisp.dhis.android.core.D2;

import io.reactivex.Completable;
import timber.log.Timber;

import static org.dhis2.utils.ConstantsKt.*;

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
                SHARE_PREFS, Context.MODE_PRIVATE);
        int eventLimit = prefs.getInt(EVENT_MAX, EVENT_MAX_DEFAULT);
        boolean limitByOU = prefs.getBoolean(LIMIT_BY_ORG_UNIT, false);
        boolean limitByProgram = prefs.getBoolean(LIMIT_BY_PROGRAM, false);
        d2.eventModule().downloadSingleEvents(eventLimit, limitByOU, limitByProgram).call();
    }

    @Override
    public void syncAndDownloadTeis(Context context) throws Exception {
        d2.trackedEntityModule().trackedEntityInstances.upload().call();
        SharedPreferences prefs = context.getSharedPreferences(
                SHARE_PREFS, Context.MODE_PRIVATE);
        int teiLimit = prefs.getInt(TEI_MAX, TEI_MAX_DEFAULT);
        boolean limitByOU = prefs.getBoolean(LIMIT_BY_ORG_UNIT, false);
        boolean limitByProgram = prefs.getBoolean(LIMIT_BY_PROGRAM, false);
        Completable.fromObservable(d2.trackedEntityModule()
                .downloadTrackedEntityInstances(teiLimit, limitByOU, limitByProgram)
                .asObservable()
                .doOnNext(data -> Timber.d(data.percentage() + "% " + data.doneCalls().size() + "/" + data.totalCalls())))
                .blockingAwait();
    }

    @Override
    public void syncMetadata(Context context) throws Exception {
        d2.syncMetaData().call();
    }

    @Override
    public void syncReservedValues() {
        d2.trackedEntityModule().reservedValueManager.syncReservedValues(null, null, 100);
    }
}
