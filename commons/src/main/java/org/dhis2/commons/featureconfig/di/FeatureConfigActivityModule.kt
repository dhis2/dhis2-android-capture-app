package org.dhis2.commons.featureconfig.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.ui.FeatureConfigViewModelFactory

@Module
class FeatureConfigActivityModule {

    @Provides
    @PerActivity
    fun provideViewModelFactory(
        featureConfigRepository: FeatureConfigRepository,
    ): FeatureConfigViewModelFactory {
        return FeatureConfigViewModelFactory(featureConfigRepository)
    }
}
