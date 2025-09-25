package org.dhis2.mobile.commons.reporting

interface CrashReportController {
    fun init()

    fun close()

    fun trackUser(
        user: String?,
        server: String?,
    )

    fun trackServer(
        server: String?,
        serverDhisVersion: String?,
    )

    fun trackError(
        exception: Exception,
        message: String?,
    )

    fun addBreadCrumb(
        category: String,
        message: String,
    )
}
