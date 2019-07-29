package org.dhis2.data.service;

import android.content.Context;
import android.content.SharedPreferences;

import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.Collections;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import timber.log.Timber;

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
                .andThen(Completable.fromObservable(d2.eventModule().downloadSingleEvents(eventLimit, limitByOU, limitByProgram))).blockingAwait();

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
                .downloadTrackedEntityInstances(teiLimit, limitByOU, limitByProgram)
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
    public void syncMetadata(Context context) {
        Completable.fromObservable(d2.syncMetaData()
                .doOnNext(data -> Timber.d(data.percentage() + "% " + data.doneCalls().size() + "/" + data.totalCalls())))
                .blockingAwait();
    }

    @Override
    public void syncReservedValues() {
        d2.trackedEntityModule().reservedValueManager.syncReservedValues(null, null, 100);
    }

    @Override
    public boolean checkSyncStatus() {
        boolean eventsOk = d2.eventModule().events.byState().notIn(State.SYNCED).get().isEmpty();
        boolean teiOk = d2.trackedEntityModule().trackedEntityInstances.byState().notIn(State.SYNCED,State.RELATIONSHIP).get().isEmpty();
        return eventsOk && teiOk;
    }

    @Override
    public Observable<D2Progress> syncGranularEvent(String eventUid) {
        return d2.eventModule().events.byUid().eq(eventUid).upload();
    }

    @Override
    public Observable<D2Progress> syncGranularProgram(String uid){
        return d2.programModule().programs.uid(uid).getAsync().toObservable()
                .flatMap(program -> {
                    if(program.programType() == ProgramType.WITH_REGISTRATION)
                        return d2.trackedEntityModule().trackedEntityInstances.byProgramUids(Collections.singletonList(uid)).upload();
                    else
                        return d2.eventModule().events.byProgramUid().eq(uid).upload();
                });
    }

    @Override
    public Observable<D2Progress> syncGranularTEI(String uid){
        return d2.trackedEntityModule().trackedEntityInstances.byUid().eq(uid).upload();
    }

    @Override
    public Observable<D2Progress> syncGranularDataSet(String uid){
        return d2.dataValueModule().dataSetReports.byDataSetUid().eq(uid).getAsync().toObservable()
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
    public Observable<D2Progress> syncGranularDataValues(String orgUnit, String attributeOptionCombo, String period){
        return d2.dataValueModule().dataValues
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriod().eq(period)
                .upload();
    }
}
