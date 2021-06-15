package org.dhis2.usescases.featureconfig

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceProvider

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
