package org.dhis2.commons.reporting

interface CrashReportController {

    fun trackScreenName(screen: String)

    fun trackUser(user: String?, server: String?)

    fun trackServer(server: String?)

    fun trackError(exception: Exception, message: String?)

    fun logMessage(message: String)

    fun addBreadCrumb(category: String, message: String)
}
