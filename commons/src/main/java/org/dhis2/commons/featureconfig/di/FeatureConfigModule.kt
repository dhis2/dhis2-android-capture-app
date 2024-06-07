package org.dhis2.commons.featureconfig.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.data.FeatureConfigRepositoryImpl
import org.dhis2.commons.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2Manager

@Module
class FeatureConfigModule {

    @Provides
    fun provideRepository(preferenceProvider: PreferenceProvider): FeatureConfigRepository {
        return FeatureConfigRepositoryImpl(preferenceProvider, provideD2())
    }

    private fun provideD2() = D2Manager.getD2()
}
