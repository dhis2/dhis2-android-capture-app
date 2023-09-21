package org.dhis2.android.rtsm.services.scheduler;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0016J\b\u0010\u0005\u001a\u00020\u0004H\u0016J\b\u0010\u0006\u001a\u00020\u0004H\u0016\u00a8\u0006\u0007"}, d2 = {"Lorg/dhis2/android/rtsm/services/scheduler/SchedulerProviderImpl;", "Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;", "()V", "computation", "Lio/reactivex/Scheduler;", "io", "ui", "psm-v2.9-DEV_debug"})
public final class SchedulerProviderImpl implements org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider {
    
    @javax.inject.Inject
    public SchedulerProviderImpl() {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.Scheduler computation() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.Scheduler io() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.Scheduler ui() {
        return null;
    }
}