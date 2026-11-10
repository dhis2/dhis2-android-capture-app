package org.dhis2.mobile.extension.host.domain

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.extension.host.data.CustomFormRepository
import org.dhis2.mobile.extension.host.model.Extension
import org.dhis2.mobile.extension.host.model.FailureReason
import org.dhis2.mobile.extension.host.renderer.CustomFormRenderer
import org.dhis2.mobile.extension.host.reporting.ExtensionFailureReporter

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
    /** SPIKE ONLY: debug builds skip the https rule. Not part of the real development. */
    private val allowHttp: Boolean = false,
) {
    suspend operator fun invoke(dataSetUid: String): ResolvedCustomForm? {
        val extension = Extension.customForm(dataSetUid)
        val (html, https) =
            try {
                val html = repository.customFormHtml(dataSetUid) ?: return null
                html to repository.serverUsesHttps()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                reporter.report(extension, FailureReason.CONTENT_UNREADABLE)
                return null
            }

        val failure =
            when {
                html.isBlank() -> FailureReason.EMPTY_CONTENT
                !https && !allowHttp -> FailureReason.INSECURE_SERVER
                else -> renderer.deviceSupport() ?: rules.firstNotNullOfOrNull { it.check(html) }
            }
        if (failure != null) {
            reporter.report(extension, failure)
            return null
        }

        return ResolvedCustomForm(extension, html)
    }
}
