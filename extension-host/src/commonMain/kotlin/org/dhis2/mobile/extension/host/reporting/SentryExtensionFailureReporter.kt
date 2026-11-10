package org.dhis2.mobile.extension.host.reporting

import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.mobile.extension.host.model.Extension
import org.dhis2.mobile.extension.host.model.FailureReason

/**
 * Sends every failure to Sentry and to the log. The event carries the kind, the target and the
 * reason, never the form HTML or data values. Sentry groups the repeats by the exception
 * message (kind and reason).
 */
class SentryExtensionFailureReporter(
    private val crashReportController: CrashReportController,
    private val log: (String) -> Unit = {},
) : ExtensionFailureReporter {
    override fun report(
        extension: Extension,
        reason: FailureReason,
    ) {
        val message = "extension ${extension.id} failed: $reason"
        log(message)
        crashReportController.trackError(ExtensionFailedException(extension, reason), message)
    }
}

class ExtensionFailedException(
    extension: Extension,
    reason: FailureReason,
) : Exception("${extension.kind} failed: $reason")
