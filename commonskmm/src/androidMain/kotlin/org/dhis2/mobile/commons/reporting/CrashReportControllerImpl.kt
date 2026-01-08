package org.dhis2.mobile.commons.reporting

import android.content.Context
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.SentryOptions.BeforeSendCallback
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import org.dhis2.mobile.commons.BuildConfig
import org.hisp.dhis.android.core.D2Manager
import timber.log.Timber

const val DATA_STORE_CRASH_PERMISSION_KEY = "analytics_permission"

class CrashReportControllerImpl(
    private val context: Context,
) : CrashReportController {
    override fun init() {
        SentryAndroid.init(context) { options: SentryAndroidOptions? ->
            options!!.setDsn(BuildConfig.SENTRY_DSN)
            options.isAnrReportInDebug = true
            options.beforeSend =
                BeforeSendCallback { event, _ ->
                    if (SentryLevel.DEBUG == event.level) null else event
                }
            options.environment = if (BuildConfig.DEBUG) "debug" else "production"
            options.isDebug = BuildConfig.DEBUG
            options.isAttachViewHierarchy = true
            options.setTracesSampleRate(if (BuildConfig.DEBUG) 1.0 else 0.1)
            options.setProfilesSampleRate(if (BuildConfig.DEBUG) 1.0 else 0.1)
        }
    }

    override fun close() {
        Sentry.close()
    }

    override fun trackServer(
        server: String?,
        serverDhisVersion: String?,
    ) {
        if (isCrashReportPermissionGranted()) {
            Sentry.configureScope { scope ->
                scope.setTag(SERVER_NAME, server ?: "")
                scope.setTag(SERVER_VERSION, serverDhisVersion ?: "")
            }
        }
    }

    override fun trackError(
        exception: Exception,
        message: String?,
    ) {
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

    override fun addBreadCrumb(
        category: String,
        message: String,
    ) {
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

    private fun isCrashReportPermissionGranted(): Boolean =
        (
            D2Manager.isD2Instantiated() &&
                D2Manager
                    .getD2()
                    .dataStoreModule()
                    .localDataStore()
                    .value(DATA_STORE_CRASH_PERMISSION_KEY)
                    .blockingGet()
                    ?.value()
                    ?.toBoolean() == true
        ).also { granted ->
            if (!granted) {
                Timber.d("Tracking is disabled")
            }
        }
}
