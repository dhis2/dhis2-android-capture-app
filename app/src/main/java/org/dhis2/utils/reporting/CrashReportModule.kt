package org.dhis2.utils.reporting

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.dhis2.commons.reporting.CrashReportController

@Module
class CrashReportModule internal constructor() {

    @Provides
    @Singleton
    fun provideCrashReportController(): CrashReportController {
        return CrashReportControllerImpl()
    }
}
