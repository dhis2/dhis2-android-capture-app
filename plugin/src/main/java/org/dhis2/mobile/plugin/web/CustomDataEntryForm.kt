package org.dhis2.mobile.plugin.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.dhis2.mobile.plugin.api.PluginApiRouter
import org.dhis2.mobile.plugin.data.CustomFormRepository
import org.dhis2.mobile.plugin.domain.model.CustomFormContext
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.koin.compose.koinInject

private const val CUSTOM_FORM_URL = "$ORIGIN/host/dhis2-plugin-host/custom-form.html"

/** The cached custom form HTML of [dataSetUid]: null while it is read, or if the data set has none. */
@Composable
fun rememberCustomFormHtml(dataSetUid: String): String? {
    val repository = koinInject<CustomFormRepository>()
    val html by produceState<String?>(initialValue = null, dataSetUid) { value = repository.html(dataSetUid) }
    return html
}

/**
 * Renders a data set's custom form, the HTML the web Data Entry app renders, for one instance.
 *
 * The form runs in a WebView that loads only local content. Its fields read and write the
 * instance's values through the SDK, and its own `/api/` calls are answered offline by
 * [PluginApiRouter]. `custom-form.html` provides the legacy custom form environment the form's
 * scripts expect: jQuery and the `dhis2.util.on` / `dhis2.de.event` hooks.
 *
 * @param editable Whether values can be changed; false once completed or out of the user's scope.
 */
@Composable
fun CustomDataEntryForm(
    html: String,
    context: CustomFormContext,
    editable: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!WebPluginSupport.isAvailable()) {
        InfoBar(
            modifier = modifier,
            text = "This custom form needs a newer Android System WebView",
            icon = {},
            textColor = SurfaceColor.Warning,
            backgroundColor = SurfaceColor.WarningContainer,
        )
        return
    }

    val repository = koinInject<CustomFormRepository>()
    val router = koinInject<PluginApiRouter>()
    val scope = rememberCoroutineScope()
    val session =
        remember(html, context) {
            CustomFormSession(
                context = context,
                load = { repository.formData(context, html) },
                save = { field, value -> repository.save(context, field, value) },
                router = router,
                scope = scope,
            )
        }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { viewContext -> hostWebView(viewContext, CUSTOM_FORM_URL, session::onMessage, zoomable = true) },
        update = { session.updateEditable(editable) },
        onRelease = { it.destroy() },
    )
}
