package org.dhis2.utils.reporting

import io.sentry.Sentry
import javax.inject.Inject

class CrashReportControllerImpl @Inject constructor() : CrashReportController {

    override fun trackScreenName(screen: String) {}

    override fun trackUser(user: String?, server: String?) {
        val sentryUser = io.sentry.protocol.User().apply {
            this.username = username
            others?.put(SERVER_NAME, server)
        }
        Sentry.setUser(sentryUser)
    }

    override fun trackServer(server: String?) {
        Sentry.configureScope { scope ->
            scope.setTag(SERVER_NAME, server ?: "")
        }
    }

    override fun logException(exception: Exception) {
//        Sentry.captureException(exception)
    }

    override fun logMessage(message: String) {
//        Sentry.captureMessage(message)
    }

    companion object {
        const val SERVER_NAME = "server_name"
    }
}
