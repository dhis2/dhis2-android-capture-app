package org.dhis2.mobile.extension.web.customform

import android.webkit.WebView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.delay
import org.dhis2.mobile.extension.host.model.DataSetInstanceRef
import org.dhis2.mobile.extension.host.model.Extension
import org.dhis2.mobile.extension.host.model.FailureReason
import org.dhis2.mobile.extension.host.renderer.CustomFormRenderer
import org.dhis2.mobile.extension.host.reporting.ExtensionFailureReporter
import org.dhis2.mobile.extension.web.api.ExtensionApiRouter
import org.dhis2.mobile.extension.web.host.HOST_PAGES_URL
import org.dhis2.mobile.extension.web.host.WebViewSupport
import org.dhis2.mobile.extension.web.host.hostWebView
import timber.log.Timber

private const val CUSTOM_FORM_URL = "$HOST_PAGES_URL/custom-form.html"

/** How long the form has to finish loading before the native form is shown instead. */
private const val RENDER_TIMEOUT_MILLIS = 10_000L

/**
 * Runs a data set custom form, the HTML the web Data Entry app renders, in the WebView host.
 *
 * `custom-form.html` provides the legacy environment the form's scripts expect: jQuery and the
 * `dhis2.util.on` / `dhis2.de.event` hooks. The form reads only its own instance, through
 * [CustomFormDataRepository]; it writes only through `saveValue`, the data set screen's save; and
 * its own `/api/` calls are refused (501), as custom forms may not read anything else.
 */
class WebViewCustomFormRenderer(
    private val repository: CustomFormDataRepository,
    private val router: ExtensionApiRouter,
    private val reporter: ExtensionFailureReporter,
) : CustomFormRenderer {
    override fun deviceSupport(): FailureReason? = if (WebViewSupport.isAvailable()) null else FailureReason.WEBVIEW_NOT_SUPPORTED

    @Composable
    override fun Render(
        html: String,
        instance: DataSetInstanceRef,
        modifier: Modifier,
        contentPadding: PaddingValues,
        editable: Boolean,
        reloadKey: Int,
        saveValue: suspend (dataElementUid: String, categoryOptionComboUid: String, value: String?) -> String?,
        onFailed: (FailureReason) -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        val currentSaveValue by rememberUpdatedState(saveValue)
        val currentOnFailed by rememberUpdatedState(onFailed)
        val state = remember(html, instance) { RenderState() }
        val fail: (FailureReason) -> Unit = { reason -> if (state.failOnce()) currentOnFailed(reason) }

        val session =
            remember(html, instance) {
                CustomFormSession(
                    instance = instance,
                    load = { repository.formData(instance, html) },
                    save = { field, value ->
                        val rejection = currentSaveValue(field.dataElement, field.categoryOptionCombo, value)
                        if (rejection != null) {
                            Result.failure(IllegalArgumentException(rejection))
                        } else {
                            repository.indicators(instance)
                        }
                    },
                    answerApi = router::notAvailable,
                    scope = scope,
                    onRendered = { state.rendered = true },
                    onScriptError = { message ->
                        Timber.tag("ExtensionHost").w("Script error in the custom form of %s: %s", instance.dataSetUid, message)
                        reporter.report(Extension.customForm(instance.dataSetUid), FailureReason.SCRIPT_ERROR)
                    },
                )
            }

        LaunchedEffect(session) {
            delay(RENDER_TIMEOUT_MILLIS)
            if (!state.rendered) {
                fail(FailureReason.RENDER_TIMEOUT)
                // A form stuck in a script keeps the renderer busy after its WebView is gone, and
                // the app's WebViews share that renderer: the next form or plugin would stay blank.
                state.webView?.terminateRenderer()
            }
        }
        LaunchedEffect(session, reloadKey) { session.reload() }

        AndroidView(
            modifier = modifier.fillMaxSize().padding(contentPadding),
            factory = { context ->
                hostWebView(
                    context = context,
                    startUrl = CUSTOM_FORM_URL,
                    onMessage = session::onMessage,
                    zoomable = true,
                    onRendererGone = { fail(FailureReason.RENDERER_CRASHED) },
                ).also { state.webView = it }
            },
            update = { session.updateEditable(editable) },
            onRelease = {
                state.webView = null
                it.destroy()
            },
        )
    }

    private fun WebView.terminateRenderer() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_TERMINATE)) {
            WebViewCompat.getWebViewRenderProcess(this)?.terminate()
        }
    }

    private class RenderState {
        var rendered = false
        var webView: WebView? = null
        private var failed = false

        /** True the first time only: the host is told once that the form cannot be shown. */
        fun failOnce(): Boolean = !failed.also { failed = true }
    }
}
