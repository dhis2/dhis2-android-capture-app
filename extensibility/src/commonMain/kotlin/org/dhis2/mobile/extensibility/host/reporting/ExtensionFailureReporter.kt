package org.dhis2.mobile.extensibility.host.reporting

import org.dhis2.mobile.extensibility.host.model.Extension
import org.dhis2.mobile.extensibility.host.model.FailureReason

/**
 * Told every time an extension cannot be shown or fails. Nothing is remembered between opens,
 * so the same failure is reported again the next time.
 */
fun interface ExtensionFailureReporter {
    fun report(
        extension: Extension,
        reason: FailureReason,
    )
}
