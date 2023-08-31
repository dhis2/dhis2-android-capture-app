package org.dhis2.commons.reporting

interface CrashReportController {

    fun trackUser(user: String?, server: String?)

    fun trackServer(server: String?, serverDhisVersion: String?)

    fun trackError(exception: Exception, message: String?)

    fun addBreadCrumb(category: String, message: String)
}
