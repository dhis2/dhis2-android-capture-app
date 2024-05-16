package org.dhis2.commons.prefs

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


    @Provides
    @Singleton
    open fun basicPreferenceProvider(context: Context): BasicPreferenceProvider {
        return BasicPreferenceProviderImpl(context)
    }
}
