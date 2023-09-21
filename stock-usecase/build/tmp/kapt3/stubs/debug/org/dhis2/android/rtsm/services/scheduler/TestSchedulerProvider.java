package org.dhis2.android.rtsm.services.scheduler;

import io.reactivex.schedulers.TestScheduler;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0005\u001a\u00020\u0003H\u0016J\b\u0010\u0006\u001a\u00020\u0003H\u0016J\b\u0010\u0007\u001a\u00020\u0003H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lorg/dhis2/android/rtsm/services/scheduler/TestSchedulerProvider;", "Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;", "scheduler", "Lio/reactivex/schedulers/TestScheduler;", "(Lio/reactivex/schedulers/TestScheduler;)V", "computation", "io", "ui", "psm-v2.9-DEV_debug"})
public final class TestSchedulerProvider implements org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider {
    @org.jetbrains.annotations.NotNull
    private final io.reactivex.schedulers.TestScheduler scheduler = null;
    
    public TestSchedulerProvider(@org.jetbrains.annotations.NotNull
    io.reactivex.schedulers.TestScheduler scheduler) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.schedulers.TestScheduler computation() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.schedulers.TestScheduler io() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.schedulers.TestScheduler ui() {
        return null;
    }
}