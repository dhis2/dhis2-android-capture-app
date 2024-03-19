package org.dhis2

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.resources.ColorUtils
import javax.inject.Singleton

@Module
class AppModule(private val application: App) {
    @Provides
    @Singleton
    fun context(): Context {
        return application
    }

    @Provides
    @Singleton
    fun colorUtils(): ColorUtils {
        return ColorUtils()
    }
}
