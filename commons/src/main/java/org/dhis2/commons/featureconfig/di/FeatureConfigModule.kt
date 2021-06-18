package org.dhis2.commons.featureconfig.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.data.FeatureConfigRepositoryImpl

@Module
class FeatureConfigModule {

    @Provides
//    @PerActivity
    fun provideRepository(
        preferenceProvider: PreferenceProvider
    ): FeatureConfigRepository {
        return FeatureConfigRepositoryImpl(preferenceProvider)
    }
}
