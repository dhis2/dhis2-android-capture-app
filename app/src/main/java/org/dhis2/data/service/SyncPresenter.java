package org.dhis2.data.service;

import android.content.Context;

import org.hisp.dhis.android.core.arch.call.D2Progress;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import io.reactivex.Observable;

interface SyncPresenter {
    void syncAndDownloadEvents(Context context) throws Exception;

    void syncAndDownloadTeis(Context context) throws Exception;

    void syncMetadata(Context context) throws Exception;

    void syncAndDownloadDataValues() throws Exception;

    void syncReservedValues();

    boolean checkSyncStatus();

    Observable<D2Progress> syncGranularEvent(String eventUid);

    Observable<D2Progress> syncGranularProgram(String uid);

    Observable<D2Progress> syncGranularTEI(String uid);

    Observable<D2Progress> syncGranularDataSet(String uid);

    Observable<D2Progress> syncGranularDataValues(String orgUnit, String attributeOptionCombo, String period);
}
