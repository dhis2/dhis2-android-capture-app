package com.dhis2.data.service;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

interface SyncPresenter {
    void sync();

    @UiThread
    void onAttach(@NonNull SyncView view);

    @UiThread
    void onDetach();
}
