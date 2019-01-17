package org.dhis2.data.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

interface SyncPresenter {
    void sync();

    void syncMetaData();

    void syncEvents();

    void syncTrackedEntities();

    void syncAggregateData();

    @UiThread
    void onAttach(@NonNull SyncView view);

    @UiThread
    void onDetach();

    void syncAndDownloadEvents(Context context) throws Exception;

    void syncAndDownloadTeis(Context context) throws Exception;

    void syncMetadata(Context context) throws Exception;
    void syncAndDownloadDataValues() throws Exception;

    void syncReservedValues();
}
