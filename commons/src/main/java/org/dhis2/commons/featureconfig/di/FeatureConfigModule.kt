package org.dhis2.commons.featureconfig.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.data.FeatureConfigRepositoryImpl
import org.dhis2.commons.prefs.PreferenceProvider

@Module
class FeatureConfigModule {

    @Provides
    fun provideRepository(preferenceProvider: PreferenceProvider): FeatureConfigRepository {
        return FeatureConfigRepositoryImpl(preferenceProvider)
    }
}
