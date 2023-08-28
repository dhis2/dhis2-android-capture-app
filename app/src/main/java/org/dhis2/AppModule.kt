package org.dhis2

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
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
    fun resources(): ResourceManager {
        return ResourceManager(application, colorUtils())
    }

    @Provides
    @Singleton
    fun colorUtils(): ColorUtils {
        return ColorUtils()
    }
}
