package org.dhis2.mobile.commons.reporting

interface CrashReportController {
    fun init()

    fun close()

    fun trackServer(serverDhisVersion: String?)

    fun trackError(
        exception: Exception,
        message: String?,
    )

    fun addBreadCrumb(
        category: String,
        message: String,
    )
}
