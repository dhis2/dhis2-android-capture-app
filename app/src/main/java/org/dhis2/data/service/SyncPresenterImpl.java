package org.dhis2.data.service;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSetInstance;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

final class SyncPresenterImpl implements SyncPresenter {

    @NonNull
    private final D2 d2;

    SyncPresenterImpl(@NonNull D2 d2) {
        this.d2 = d2;
    }

    @Override
    public void syncAndDownloadEvents(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        int eventLimit = prefs.getInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT);
        boolean limitByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        boolean limitByProgram = prefs.getBoolean(Constants.LIMIT_BY_PROGRAM, false);
        Completable.fromObservable(d2.eventModule().events.upload())
                .andThen(Completable.fromObservable(d2.eventModule()
                        .eventDownloader.limit(eventLimit).limitByOrgunit(limitByOU).limitByProgram(limitByProgram).download())
                ).blockingAwait();

    }

    @Override
    public void syncAndDownloadTeis(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        int teiLimit = prefs.getInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT);
        boolean limitByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        boolean limitByProgram = prefs.getBoolean(Constants.LIMIT_BY_PROGRAM, false);
        Completable.fromObservable(d2.trackedEntityModule().trackedEntityInstances.upload()).andThen(
                Completable.fromObservable(d2.trackedEntityModule()
                        .trackedEntityInstanceDownloader.limit(teiLimit).limitByOrgunit(limitByOU).limitByProgram(limitByProgram)
                        .download()
                        .doOnNext(data -> Timber.d(data.percentage() + "% " + data.doneCalls().size() + "/" + data.totalCalls())))
                        .doOnError(error -> Timber.d("error while downloading TEIs"))
                        .onErrorComplete())
                .blockingAwait();
    }

    @Override
    public void syncAndDownloadDataValues() {
        Completable.fromObservable(d2.dataValueModule().dataValues.upload())
                .andThen(
                        Completable.fromObservable(d2.dataSetModule().dataSetCompleteRegistrations.upload()))
                .andThen(
                        Completable.fromObservable(d2.aggregatedModule().data().download())).blockingAwait();
    }

    @Override
    public void syncMetadata(Context context, SyncMetadataWorker.OnProgressUpdate progressUpdate) {
        Completable.fromObservable(d2.metadataModule().download()
                .doOnNext(data -> progressUpdate.onProgressUpdate((int) Math.ceil(data.percentage()))))
                .blockingAwait();
    }

    @Override
    public void syncReservedValues() {
        d2.trackedEntityModule().reservedValueManager.blockingDownloadAllReservedValues(100);
    }

    @Override
    public boolean checkSyncStatus() {
        boolean eventsOk = d2.eventModule().events.byState().notIn(State.SYNCED).blockingGet().isEmpty();
        boolean teiOk = d2.trackedEntityModule().trackedEntityInstances.byState().notIn(State.SYNCED, State.RELATIONSHIP).blockingGet().isEmpty();
        return eventsOk && teiOk;
    }

    @Override
    public Observable<D2Progress> syncGranularEvent(String eventUid) {
        return d2.eventModule().events.byUid().eq(eventUid).upload();
    }

    @Override
    public Observable<D2Progress> syncGranularProgram(String uid) {
        return d2.programModule().programs.uid(uid).get().toObservable()
                .flatMap(program -> {
                    if (program.programType() == ProgramType.WITH_REGISTRATION)
                        return d2.trackedEntityModule().trackedEntityInstances.byProgramUids(Collections.singletonList(uid)).upload();
                    else
                        return d2.eventModule().events.byProgramUid().eq(uid).upload();
                });
    }

    @Override
    public Observable<D2Progress> syncGranularTEI(String uid) {
        return d2.trackedEntityModule().trackedEntityInstances.byUid().eq(uid).upload();
    }

    @Override
    public Observable<D2Progress> syncGranularDataSet(String uid) {
        return d2.dataSetModule().dataSetInstances.byDataSetUid().eq(uid).get().toObservable()
                .flatMapIterable(dataSets -> dataSets)
                .flatMap(dataSetReport ->
                        d2.dataValueModule().dataValues
                                .byOrganisationUnitUid().eq(dataSetReport.attributeOptionComboUid())
                                .byPeriod().eq(dataSetReport.period())
                                .byAttributeOptionComboUid().eq(dataSetReport.attributeOptionComboUid())
                                .upload()
                );
    }

    @Override
    public Observable<D2Progress> syncGranularDataValues(String orgUnit, String attributeOptionCombo, String period) {
        return d2.dataValueModule().dataValues
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriod().eq(period)
                .upload();
    }

    @Override
    public boolean checkSyncEventStatus(String uid) {
        return d2.eventModule().events
                .byUid().eq(uid)
                .byState().notIn(State.SYNCED)
                .blockingGet().isEmpty();
    }

    @Override
    public boolean checkSyncTEIStatus(String uid) {
        return d2.trackedEntityModule().trackedEntityInstances
                .byUid().eq(uid)
                .byState().notIn(State.SYNCED, State.RELATIONSHIP)
                .blockingGet().isEmpty();
    }

    @Override
    public boolean checkSyncDataValueStatus(String orgUnit, String attributeOptionCombo, String period) {
        return d2.dataValueModule().dataValues.byPeriod().eq(period)
                .byOrganisationUnitUid().eq(orgUnit)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .byState().notIn(State.SYNCED)
                .blockingGet().isEmpty();
    }

    @Override
    public boolean checkSyncProgramStatus(String uid) {
        Program program = d2.programModule().programs.uid(uid).blockingGet();

        if (program.programType() == ProgramType.WITH_REGISTRATION)
            return d2.trackedEntityModule().trackedEntityInstances
                    .byProgramUids(Collections.singletonList(uid))
                    .byState().notIn(State.SYNCED, State.RELATIONSHIP)
                    .blockingGet().isEmpty();
        else
            return d2.eventModule().events.byProgramUid().eq(uid)
                    .byState().notIn(State.SYNCED)
                    .blockingGet().isEmpty();

    }

    @Override
    public boolean checkSyncDataSetStatus(String uid) {
        DataSetInstance dataSetReport = d2.dataSetModule().dataSetInstances.byDataSetUid().eq(uid).one().blockingGet();

        return d2.dataValueModule().dataValues
                .byOrganisationUnitUid().eq(dataSetReport.attributeOptionComboUid())
                .byPeriod().eq(dataSetReport.period())
                .byAttributeOptionComboUid().eq(dataSetReport.attributeOptionComboUid())
                .byState().notIn(State.SYNCED)
                .blockingGet().isEmpty();
    }

    @Override
    public List<TrackerImportConflict> messageTrackerImportConflict(String uid) {
        List<TrackerImportConflict> trackerImportConflicts = d2.importModule().trackerImportConflicts.byTrackedEntityInstanceUid().eq(uid).blockingGet();
        if (trackerImportConflicts != null && !trackerImportConflicts.isEmpty())
            return trackerImportConflicts;

        trackerImportConflicts = d2.importModule().trackerImportConflicts.byEventUid().eq(uid).blockingGet();
        if (trackerImportConflicts != null && !trackerImportConflicts.isEmpty())
            return trackerImportConflicts;

        return null;
    }

    @Override
    public void startPeriodicDataWork(Context context) {
        int seconds = context.getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).getInt(Constants.TIME_DATA, Constants.TIME_DAILY);
        WorkManager.getInstance(context).cancelUniqueWork(Constants.DATA);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncDataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(Constants.DATA);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(Constants.DATA, ExistingPeriodicWorkPolicy.REPLACE, request);
    }

    @Override
    public void startPeriodicMetaWork(Context context) {
        int seconds = context.getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).getInt(Constants.TIME_META, Constants.TIME_DAILY);
        WorkManager.getInstance(context).cancelUniqueWork(Constants.META);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncDataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(Constants.META);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(Constants.META, ExistingPeriodicWorkPolicy.REPLACE, request);
    }
}
