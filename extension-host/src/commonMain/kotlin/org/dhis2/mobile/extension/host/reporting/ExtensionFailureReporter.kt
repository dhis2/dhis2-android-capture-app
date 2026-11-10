package org.dhis2.mobile.extension.host.reporting

import org.dhis2.mobile.extension.host.model.Extension
import org.dhis2.mobile.extension.host.model.FailureReason

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
