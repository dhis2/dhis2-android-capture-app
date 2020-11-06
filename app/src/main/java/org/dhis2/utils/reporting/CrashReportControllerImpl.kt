package org.dhis2.utils.reporting

import javax.inject.Inject
import org.acra.ACRA

class CrashReportControllerImpl @Inject constructor() : CrashReportController {

    override fun trackScreenName(screen: String) {
        ACRA.getErrorReporter().putCustomData(SCREEN_NAME, screen)
    }

    override fun trackUser(user: String) {
        ACRA.getErrorReporter().putCustomData(USER, user)
    }

    override fun trackServer(server: String) {
        ACRA.getErrorReporter().putCustomData(SERVER, server)
    }

    override fun logException(exception: Exception) {
        ACRA.getErrorReporter().handleException(exception)
    }

    override fun logMessage(tag: String, message: String) {
        ACRA.getErrorReporter().putCustomData(tag, message)
    }

    companion object {
        const val SCREEN_NAME = "SCREEN_NAME"
        const val SERVER = "SERVER"
        const val USER = "USER"
    }
}
