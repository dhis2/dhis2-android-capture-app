package org.dhis2.mobile.commons.reporting

import io.sentry.Breadcrumb
import io.sentry.Sentry
import org.hisp.dhis.android.core.D2Manager
import timber.log.Timber

const val DATA_STORE_CRASH_PERMISSION_KEY = "analytics_permission"

class CrashReportControllerImpl : CrashReportController {

    override fun trackUser(user: String?, server: String?) {
        if (isCrashReportPermissionGranted()) {
            val sentryUser = io.sentry.protocol.User().apply {
                user?.let { this.username = user }
                server?.let { others?.put(SERVER_NAME, server) }
            }
            Sentry.setUser(sentryUser)
        }
    }

    override fun trackServer(server: String?, serverDhisVersion: String?) {
        if (isCrashReportPermissionGranted()) {
            Sentry.configureScope { scope ->
                scope.setTag(SERVER_NAME, server ?: "")
                scope.setTag(SERVER_VERSION, serverDhisVersion ?: "")
            }
        }
    }

    override fun trackError(exception: Exception, message: String?) {
        if (isCrashReportPermissionGranted()) {
            val breadcrumb = Breadcrumb()
            message?.let {
                breadcrumb.type = "Info"
                breadcrumb.message = message
                Sentry.addBreadcrumb(breadcrumb)
            }
            Sentry.captureException(exception)
        }
    }

    override fun addBreadCrumb(category: String, message: String) {
        if (isCrashReportPermissionGranted()) {
            val breadcrumb = Breadcrumb()
            breadcrumb.type = "info"
            breadcrumb.category = category
            breadcrumb.message = message
            Sentry.addBreadcrumb(breadcrumb)
        }
    }

    companion object {
        const val SERVER_NAME = "server_name"
        const val SERVER_VERSION = "server_version"
    }

    private fun isCrashReportPermissionGranted(): Boolean {
        return (
            D2Manager.isD2Instantiated() &&
                D2Manager.getD2().dataStoreModule().localDataStore()
                    .value(DATA_STORE_CRASH_PERMISSION_KEY).blockingGet()?.value()
                    ?.toBoolean() == true
            ).also { granted ->
            if (!granted) {
                Timber.d("Tracking is disabled")
            }
        }
    }
}
