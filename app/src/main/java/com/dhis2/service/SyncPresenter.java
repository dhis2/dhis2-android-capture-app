package com.dhis2.service;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

interface SyncPresenter {
    void sync();

    @UiThread
    void onAttach(@NonNull SyncView view);

    @UiThread
    void onDetach();
}
