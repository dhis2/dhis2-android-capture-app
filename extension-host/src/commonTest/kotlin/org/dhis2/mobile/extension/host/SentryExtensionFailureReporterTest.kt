package org.dhis2.mobile.extension.host

import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.mobile.extension.host.model.Extension
import org.dhis2.mobile.extension.host.model.FailureReason
import org.dhis2.mobile.extension.host.reporting.SentryExtensionFailureReporter
import kotlin.test.Test
import kotlin.test.assertEquals

class SentryExtensionFailureReporterTest {
    private val crashes = FakeCrashReportController()
    private val logged = mutableListOf<String>()
    private val reporter = SentryExtensionFailureReporter(crashes) { logged += it }
    private val extension = Extension.customForm("BfMAe6Itzgt")

    @Test
    fun `the event carries the kind, the data set and the reason`() {
        reporter.report(extension, FailureReason.EMPTY_CONTENT)

        assertEquals(listOf("CUSTOM_FORM failed: EMPTY_CONTENT"), crashes.errors.map { it.message })
        assertEquals(listOf("extension ${extension.id} failed: EMPTY_CONTENT"), crashes.messages)
        assertEquals(crashes.messages, logged)
    }

    @Test
    fun `every failure sends its own event`() {
        repeat(2) { reporter.report(extension, FailureReason.RENDER_TIMEOUT) }

        assertEquals(2, crashes.errors.size)
    }
}

private class FakeCrashReportController : CrashReportController {
    val errors = mutableListOf<Exception>()
    val messages = mutableListOf<String>()

    override fun init() = Unit

    override fun close() = Unit

    override fun trackServer(serverDhisVersion: String?) = Unit

    override fun trackError(
        exception: Exception,
        message: String?,
    ) {
        errors += exception
        messages += message.orEmpty()
    }

    override fun addBreadCrumb(
        category: String,
        message: String,
    ) = Unit
}
