package org.dhis2.usescases.featureconfig.di

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.usescases.featureconfig.data.FeatureConfigRepository
import org.dhis2.usescases.featureconfig.data.FeatureConfigRepositoryImpl

@Module
class FeatureConfigModule {

    @Provides
    @PerActivity
    fun provideRepository(
        preferenceProvider: PreferenceProvider
    ): FeatureConfigRepository {
        return FeatureConfigRepositoryImpl(preferenceProvider)
    }
}
