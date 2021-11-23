package org.dhis2.utils.reporting

interface CrashReportController {

    fun trackScreenName(screen: String)

    fun trackUser(user: String?, server: String?)

    fun trackServer(server: String?)

    fun trackError(exception: Exception, message: String?)

    @Deprecated("Use trackError(d2Error: D2Error) instead")
    fun logException(exception: Exception)

    fun logMessage(message: String)

    fun addBreadCrumb(category: String, message: String)
}
