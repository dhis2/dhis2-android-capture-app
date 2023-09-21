package org.dhis2.android.rtsm.di;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import org.dhis2.android.rtsm.coroutines.StockDispatcherProvider;
import org.dhis2.android.rtsm.services.SpeechRecognitionManager;
import org.dhis2.android.rtsm.services.SpeechRecognitionManagerImpl;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.viewmodel.DispatcherProvider;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.D2Manager;
import javax.inject.Singleton;

@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0006H\u0007J\u001a\u0010\u0007\u001a\u00020\b2\b\b\u0001\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u0004H\u0007J\b\u0010\f\u001a\u00020\rH\u0007J\u0012\u0010\u000e\u001a\u00020\u000f2\b\b\u0001\u0010\t\u001a\u00020\nH\u0007\u00a8\u0006\u0010"}, d2 = {"Lorg/dhis2/android/rtsm/di/AppModule;", "", "()V", "provideColorUtilsProvider", "Lorg/dhis2/commons/resources/ColorUtils;", "provideDispatcherProvider", "Lorg/dhis2/commons/viewmodel/DispatcherProvider;", "provideResourcesProvider", "Lorg/dhis2/commons/resources/ResourceManager;", "appContext", "Landroid/content/Context;", "colorUtils", "providesD2", "Lorg/hisp/dhis/android/core/D2;", "providesSpeechRecognitionManager", "Lorg/dhis2/android/rtsm/services/SpeechRecognitionManager;", "psm-v2.9-DEV_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    
    public AppModule() {
        super();
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final org.hisp.dhis.android.core.D2 providesD2() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.services.SpeechRecognitionManager providesSpeechRecognitionManager(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context appContext) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.commons.resources.ResourceManager provideResourcesProvider(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    org.dhis2.commons.resources.ColorUtils colorUtils) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.commons.viewmodel.DispatcherProvider provideDispatcherProvider() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.commons.resources.ColorUtils provideColorUtilsProvider() {
        return null;
    }
}