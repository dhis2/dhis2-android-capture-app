package org.dhis2.common.preferences

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.dhis2.commons.prefs.PreferenceModule
import org.dhis2.commons.prefs.PreferenceProvider

@Module
class PreferencesTestingModule : PreferenceModule() {

    @Provides
    @Singleton
    fun providePreferenceProvider(context: Context): PreferenceProvider {
        return PreferenceTestingImpl(context)
    }
}
