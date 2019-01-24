package org.dhis2.data.service;

import android.content.Context;
import androidx.annotation.NonNull;

import io.reactivex.functions.Consumer;


interface SyncView{

    @NonNull
    Consumer<SyncResult> update(SyncState syncState);

    @NonNull
    Context getContext();
}
