package org.dhis2

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.data.service.SyncGranularWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module
import javax.inject.Singleton

@Module
class AppModule(
    private val application: App,
) {
    @Provides
    @Singleton
    fun context(): Context = application

    @Provides
    @Singleton
    fun colorUtils(): ColorUtils = ColorUtils()
}

val appModule =
    module {
        workerOf(::SyncGranularWorker)
    }
