package org.dhis2.data.service;

import android.content.Context;

interface SyncPresenter {
    void syncAndDownloadEvents(Context context) throws Exception;

    void syncAndDownloadTeis(Context context) throws Exception;

    void syncMetadata(Context context) throws Exception;

    void syncReservedValues();
}
