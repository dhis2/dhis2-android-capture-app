package org.dhis2.data.prefs

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PreferenceModule {

    @Provides
    @Singleton
    fun preferenceProvider(context: Context): PreferenceProvider {
        return PreferenceProviderImpl(context)
    }
}
