package org.dhis2.data.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

interface SyncPresenter {
    void syncAndDownloadEvents(Context context) throws Exception;

    void syncAndDownloadTeis(Context context) throws Exception;

    void syncMetadata(Context context) throws Exception;
    void syncAndDownloadDataValues() throws Exception;

    void syncReservedValues();
}
