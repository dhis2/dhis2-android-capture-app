package org.dhis2.utils.reporting

import io.sentry.Breadcrumb
import io.sentry.Sentry
import javax.inject.Inject
import org.dhis2.commons.reporting.CrashReportController

class CrashReportControllerImpl @Inject constructor() : CrashReportController {

    override fun trackScreenName(screen: String) {}

    override fun trackUser(user: String?, server: String?) {
        val sentryUser = io.sentry.protocol.User().apply {
            user?.let { this.username = user }
            server?.let { others?.put(SERVER_NAME, server) }
        }
        Sentry.setUser(sentryUser)
    }

    override fun trackServer(server: String?) {
        Sentry.configureScope { scope ->
            scope.setTag(SERVER_NAME, server ?: "")
        }
    }

    override fun trackError(exception: Exception, message: String?) {
        val breadcrumb = Breadcrumb()
        message?.let {
            breadcrumb.type = "Info"
            breadcrumb.message = message
            Sentry.addBreadcrumb(breadcrumb)
        }
        Sentry.captureException(exception)
    }

    override fun logException(exception: Exception) {
//        Sentry.captureException(exception)
    }

    override fun logMessage(message: String) {
//        Sentry.captureMessage(message)
    }

    override fun addBreadCrumb(category: String, message: String) {
        val breadcrumb = Breadcrumb()
        breadcrumb.type = "info"
        breadcrumb.category = category
        breadcrumb.message = message
        Sentry.addBreadcrumb(breadcrumb)
    }

    companion object {
        const val SERVER_NAME = "server_name"
    }
}
