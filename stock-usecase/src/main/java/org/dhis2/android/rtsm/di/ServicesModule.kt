package org.dhis2.android.rtsm.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import org.dhis2.android.rtsm.services.MetadataManager
import org.dhis2.android.rtsm.services.MetadataManagerImpl
import org.dhis2.android.rtsm.services.StockManager
import org.dhis2.android.rtsm.services.StockManagerImpl
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.rules.RuleValidationHelperImpl
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.services.scheduler.SchedulerProviderImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class ServicesModule {
    @Binds
    abstract fun providesSchedulerProvider(impl: SchedulerProviderImpl): BaseSchedulerProvider

    @Binds
    abstract fun provideMetadataManager(impl: MetadataManagerImpl): MetadataManager

    @Binds
    abstract fun provideStockManager(impl: StockManagerImpl): StockManager

    @Binds
    abstract fun provideProgramRuleValidationHelper(
        impl: RuleValidationHelperImpl,
    ): RuleValidationHelper
}
