package org.dhis2.commons.featureconfig.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.featureconfig.ui.FeatureConfigViewModelFactory
import org.dhis2.mobile.commons.featureconfig.data.FeatureConfigRepository

@Module
class FeatureConfigActivityModule {
    @Provides
    @PerActivity
    fun provideViewModelFactory(featureConfigRepository: FeatureConfigRepository): FeatureConfigViewModelFactory =
        FeatureConfigViewModelFactory(featureConfigRepository)
}
