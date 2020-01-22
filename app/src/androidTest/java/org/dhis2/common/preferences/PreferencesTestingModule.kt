package org.dhis2.common.preferences

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.data.prefs.PreferenceModule
import org.dhis2.data.prefs.PreferenceProvider
import javax.inject.Singleton

@Module
class PreferencesTestingModule : PreferenceModule() {

    @Provides
    @Singleton
    override fun preferenceProvider(context: Context): PreferenceProvider {
        return PreferenceTestingImpl(context)
    }
}