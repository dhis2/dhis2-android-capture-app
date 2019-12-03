package org.dhis2.data.service;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;

import java.util.List;

import androidx.work.ListenableWorker;

import io.reactivex.Observable;

interface SyncPresenter {
    void syncAndDownloadEvents() throws Exception;

    void syncAndDownloadTeis() throws Exception;

    void syncMetadata(SyncMetadataWorker.OnProgressUpdate progressUpdate) throws Exception;

    void syncAndDownloadDataValues() throws Exception;

    void syncReservedValues();

    boolean checkSyncStatus();

    Observable<D2Progress> syncGranularEvent(String eventUid);

    ListenableWorker.Result blockSyncGranularProgram(String programUid);

    ListenableWorker.Result blockSyncGranularTei(String teiUid);

    ListenableWorker.Result blockSyncGranularEvent(String eventUid);

    ListenableWorker.Result blockSyncGranularDataSet(String dataSetUid);

    Observable<D2Progress> syncGranularProgram(String uid);

    Observable<D2Progress> syncGranularTEI(String uid);

    Observable<D2Progress> syncGranularDataSet(String uid);

    Observable<D2Progress> syncGranularDataValues(String orgUnit, String attributeOptionCombo, String period, String[] catOptionCombos);

    Observable<D2Progress> syncGranularDataSetComplete(String dataSetUid, String orgUnit, String attributeOptionCombo, String period);

    Observable<D2Progress> syncGranularDataSetComplete(String dataSetUid);

    boolean checkSyncEventStatus(String uid);

    boolean checkSyncTEIStatus(String uid);

    boolean checkSyncDataValueStatus(String orgUnit, String attributeOptionCombo, String period);

    boolean checkSyncProgramStatus(String uid);

    boolean checkSyncDataSetStatus(String uid);

    List<TrackerImportConflict> messageTrackerImportConflict(String uid);

    void startPeriodicDataWork();

    void startPeriodicMetaWork();

    void downloadResources();

    void uploadResources();

    ListenableWorker.Result blockSyncGranularDataValues(String dataSetUid, String orgUnitUid, String attrOptionCombo, String periodId, String[] catOptionCombo);
}
