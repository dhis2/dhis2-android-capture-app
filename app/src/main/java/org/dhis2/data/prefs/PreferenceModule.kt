package org.dhis2.data.prefs

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class PreferenceModule {

    @Provides
    @Singleton
    open fun preferenceProvider(context: Context): PreferenceProvider {
        return PreferenceProviderImpl(context)
    }
}
