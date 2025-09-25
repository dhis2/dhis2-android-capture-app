package org.dhis2.commons.reporting

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.mobile.commons.reporting.CrashReportControllerImpl
import javax.inject.Singleton

@Module
class CrashReportModule internal constructor() {
    @Provides
    @Singleton
    fun provideCrashReportController(context: Context): CrashReportController = CrashReportControllerImpl(context)
}
