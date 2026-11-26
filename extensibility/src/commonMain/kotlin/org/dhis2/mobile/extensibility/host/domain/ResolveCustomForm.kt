package org.dhis2.mobile.extensibility.host.domain

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.extensibility.host.data.CustomFormRepository
import org.dhis2.mobile.extensibility.host.model.Extension
import org.dhis2.mobile.extensibility.host.model.FailureReason
import org.dhis2.mobile.extensibility.host.renderer.CustomFormRenderer
import org.dhis2.mobile.extensibility.host.reporting.ExtensionFailureReporter

data class ResolvedCustomForm(
    val extension: Extension,
    val html: String,
)

/**
 * Decides, each time a data set opens, whether it opens with its custom form. Returns null for
 * "use the native form": no custom form (the default), or a form that cannot be shown, which is
 * reported. Nothing is stored between opens.
 *
 * Never throws: one extension's failure must not break the screen or other extensions.
 */
class ResolveCustomForm(
    private val repository: CustomFormRepository,
    private val renderer: CustomFormRenderer,
    private val rules: List<CompatibilityRule<String>>,
    private val reporter: ExtensionFailureReporter,
) {
    suspend operator fun invoke(dataSetUid: String): ResolvedCustomForm? {
        val extension = Extension.CustomForm(dataSetUid)
        val html =
            try {
                repository.customFormHtml(dataSetUid)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }

        val failure =
            when {
                html == null -> FailureReason.CONTENT_UNREADABLE
                html.isBlank() -> FailureReason.EMPTY_CONTENT
                !repository.isSecureServer() -> FailureReason.INSECURE_SERVER
                renderer.deviceSupported() -> FailureReason.UNSUPPORTED_FEATURE
                else -> rules.firstNotNullOfOrNull { it.check(html) }
            }
        return if (failure != null) {
            reporter.report(extension, failure)
            null
        } else {
            html?.let { ResolvedCustomForm(extension, it) }
        }
    }
}
