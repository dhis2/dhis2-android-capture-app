package com.dhis2.service;

import android.support.annotation.NonNull;

import io.reactivex.functions.Consumer;

interface SyncView{

    @NonNull
    Consumer<SyncResult> update();
}
