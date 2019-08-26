package org.dhis2.data.sharedPreferences

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@Singleton
class SharedPreferencesModule {
    @Provides
    @Singleton
    fun sharedPreferencesProvider(context: Context): SharePreferencesProvider {
        return SharePreferencesProviderImpl(context)
    }
}