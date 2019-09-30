package org.dhis2.data.service;

import android.content.Context;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import io.reactivex.Observable;

interface SyncPresenter {
    void syncAndDownloadEvents(Context context) throws Exception;

    void syncAndDownloadTeis(Context context) throws Exception;

    void syncMetadata(Context context, SyncMetadataWorker.OnProgressUpdate progressUpdate) throws Exception;

    void syncAndDownloadDataValues() throws Exception;

    void syncReservedValues();

    boolean checkSyncStatus();

    Observable<D2Progress> syncGranularEvent(String eventUid);

    Observable<D2Progress> syncGranularProgram(String uid);

    Observable<D2Progress> syncGranularTEI(String uid);

    Observable<D2Progress> syncGranularDataSet(String uid);

    Observable<D2Progress> syncGranularDataValues(String orgUnit, String attributeOptionCombo, String period);

    boolean checkSyncEventStatus(String uid);

    boolean checkSyncTEIStatus(String uid);

    boolean checkSyncDataValueStatus(String orgUnit, String attributeOptionCombo, String period);

    boolean checkSyncProgramStatus(String uid);

    boolean checkSyncDataSetStatus(String uid);

    List<TrackerImportConflict> messageTrackerImportConflict(String uid);

    void startPeriodicDataWork(Context context);

    void startPeriodicMetaWork(Context context);

    void downloadResources();

    void uploadResources();
}
