package com.data.schedulers;

import android.support.annotation.NonNull;

import io.reactivex.Scheduler;

public interface SchedulerProvider {

    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();

    /**
     * Legacy scheduler needed for SqlBrite. Remove this when SqlBrite lib moves to rxJava 2.x
     */
    @NonNull
    rx.Scheduler legacyIo();
}
