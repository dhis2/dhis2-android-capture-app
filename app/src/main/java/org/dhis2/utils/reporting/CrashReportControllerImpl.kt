package org.dhis2.utils.reporting

import javax.inject.Inject

class CrashReportControllerImpl @Inject constructor() : CrashReportController {

    override fun trackScreenName(screen: String) {}

    override fun trackUser(user: String) {}

    override fun trackServer(server: String) {}

    override fun logException(exception: Exception) {}

    override fun logMessage(tag: String, message: String) {}

    companion object {
        const val SCREEN_NAME = "SCREEN_NAME"
        const val SERVER = "SERVER"
        const val USER = "USER"
    }
}
