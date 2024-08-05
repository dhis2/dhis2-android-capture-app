package org.dhis2.mobile.myplugin.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PluginModule {

    @Provides
    @Singleton
    fun providesD2(): D2 {
        return D2Manager.getD2()
    }
}
