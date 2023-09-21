package org.dhis2.android.rtsm.services.scheduler;

import io.reactivex.Scheduler;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0003H&J\b\u0010\u0005\u001a\u00020\u0003H&\u00a8\u0006\u0006"}, d2 = {"Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;", "", "computation", "Lio/reactivex/Scheduler;", "io", "ui", "psm-v2.9-DEV_debug"})
public abstract interface BaseSchedulerProvider {
    
    @org.jetbrains.annotations.NotNull
    public abstract io.reactivex.Scheduler computation();
    
    @org.jetbrains.annotations.NotNull
    public abstract io.reactivex.Scheduler io();
    
    @org.jetbrains.annotations.NotNull
    public abstract io.reactivex.Scheduler ui();
}