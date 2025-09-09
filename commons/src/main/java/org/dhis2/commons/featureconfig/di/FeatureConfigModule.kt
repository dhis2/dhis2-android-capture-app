package org.dhis2.commons.featureconfig.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.data.FeatureConfigRepositoryImpl
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager

@Module
class FeatureConfigModule {
    @Provides
    fun provideRepository(): FeatureConfigRepository = FeatureConfigRepositoryImpl(provideD2())

    private fun provideD2(): D2 = D2Manager.getD2()
}
