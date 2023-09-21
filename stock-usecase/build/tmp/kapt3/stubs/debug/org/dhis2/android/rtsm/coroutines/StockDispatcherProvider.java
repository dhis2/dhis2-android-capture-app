package org.dhis2.android.rtsm.coroutines;

import kotlinx.coroutines.Dispatchers;
import org.dhis2.commons.viewmodel.DispatcherProvider;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0016J\b\u0010\u0005\u001a\u00020\u0004H\u0016J\b\u0010\u0006\u001a\u00020\u0007H\u0016\u00a8\u0006\b"}, d2 = {"Lorg/dhis2/android/rtsm/coroutines/StockDispatcherProvider;", "Lorg/dhis2/commons/viewmodel/DispatcherProvider;", "()V", "computation", "Lkotlinx/coroutines/CoroutineDispatcher;", "io", "ui", "Lkotlinx/coroutines/MainCoroutineDispatcher;", "psm-v2.9-DEV_debug"})
public final class StockDispatcherProvider implements org.dhis2.commons.viewmodel.DispatcherProvider {
    
    public StockDispatcherProvider() {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.CoroutineDispatcher io() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.CoroutineDispatcher computation() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.MainCoroutineDispatcher ui() {
        return null;
    }
}