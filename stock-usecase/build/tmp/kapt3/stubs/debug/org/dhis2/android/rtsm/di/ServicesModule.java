package org.dhis2.android.rtsm.di;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;
import org.dhis2.android.rtsm.services.MetadataManager;
import org.dhis2.android.rtsm.services.MetadataManagerImpl;
import org.dhis2.android.rtsm.services.StockManager;
import org.dhis2.android.rtsm.services.StockManagerImpl;
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper;
import org.dhis2.android.rtsm.services.rules.RuleValidationHelperImpl;
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider;
import org.dhis2.android.rtsm.services.scheduler.SchedulerProviderImpl;

@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\tH\'J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\fH\'J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u000fH\'\u00a8\u0006\u0010"}, d2 = {"Lorg/dhis2/android/rtsm/di/ServicesModule;", "", "()V", "provideMetadataManager", "Lorg/dhis2/android/rtsm/services/MetadataManager;", "impl", "Lorg/dhis2/android/rtsm/services/MetadataManagerImpl;", "provideProgramRuleValidationHelper", "Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelper;", "Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelperImpl;", "provideStockManager", "Lorg/dhis2/android/rtsm/services/StockManager;", "Lorg/dhis2/android/rtsm/services/StockManagerImpl;", "providesSchedulerProvider", "Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;", "Lorg/dhis2/android/rtsm/services/scheduler/SchedulerProviderImpl;", "psm-v2.9-DEV_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.android.components.ViewModelComponent.class})
public abstract class ServicesModule {
    
    public ServicesModule() {
        super();
    }
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider providesSchedulerProvider(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.scheduler.SchedulerProviderImpl impl);
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract org.dhis2.android.rtsm.services.MetadataManager provideMetadataManager(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.MetadataManagerImpl impl);
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract org.dhis2.android.rtsm.services.StockManager provideStockManager(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.StockManagerImpl impl);
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract org.dhis2.android.rtsm.services.rules.RuleValidationHelper provideProgramRuleValidationHelper(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.rules.RuleValidationHelperImpl impl);
}