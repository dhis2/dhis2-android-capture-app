package org.dhis2.data.schedulers;

import android.support.annotation.NonNull;

import io.reactivex.Scheduler;

public interface SchedulerProvider {

    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();
}
