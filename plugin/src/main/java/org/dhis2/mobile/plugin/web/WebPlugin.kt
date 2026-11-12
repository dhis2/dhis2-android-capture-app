package org.dhis2.mobile.plugin.web

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.dhis2.mobile.extension.web.api.ExtensionApiRouter
import org.dhis2.mobile.extension.web.host.BUNDLES_URL
import org.dhis2.mobile.extension.web.host.ORIGIN
import org.dhis2.mobile.extension.web.host.WebViewSupport
import org.dhis2.mobile.extension.web.host.hostWebView
import org.dhis2.mobile.plugin.di.WEB_PLUGINS_DIR
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.koin.compose.koinInject
import java.io.File

private const val BRIDGE_URL = "$ORIGIN/host/dhis2-plugin-host/bridge.html"
private const val SET_PLUGIN_HEIGHT = "setPluginHeight"

/**
 * Renders an unmodified DHIS2 web plugin from its bundle on the device.
 *
 * Everything the WebView loads comes from the app: the bridge page from the APK's assets, the
 * plugin from its extracted bundle, and every `/api/` call from [ExtensionApiRouter] over the
 * `dhis2Native` message bridge. Requests to any other host are refused, and so is navigating
 * away, so a plugin renders and works the same with no connection at all.
 *
 * @param props Passed to the plugin as its props, and pushed again whenever they change.
 * @param onCallback Called when the plugin invokes a function prop, such as `setFieldValue`.
 * @param initialHeight Height until the plugin reports its own through `setPluginHeight`.
 */
@Composable
fun WebPlugin(
    plugin: RegisteredPlugin,
    modifier: Modifier = Modifier,
    props: JsonObject = JsonObject(emptyMap()),
    onCallback: (PluginCallback) -> Unit = {},
    initialHeight: Dp = 240.dp,
    maxHeight: Dp = 800.dp,
) {
    if (!WebViewSupport.isAvailable()) {
        InfoBar(
            modifier = modifier,
            text = "Plugin '${plugin.key}' needs a newer Android System WebView",
            icon = {},
            textColor = SurfaceColor.Warning,
            backgroundColor = SurfaceColor.WarningContainer,
        )
        return
    }

    val router = koinInject<ExtensionApiRouter>()
    val scope = rememberCoroutineScope()
    val currentOnCallback by rememberUpdatedState(onCallback)
    var height by remember(plugin) { mutableStateOf(initialHeight) }

    val session =
        remember(plugin) {
            PluginBridgeSession(
                pluginUrl = "$BUNDLES_URL/${plugin.bundleDir.name}/${plugin.entry}",
                answerApi = router::handle,
                scope = scope,
                onCallback = { callback ->
                    if (callback.name == SET_PLUGIN_HEIGHT) {
                        callback.args.jsonPrimitive.doubleOrNull?.let {
                            height = it.dp.coerceIn(1.dp, maxHeight)
                        }
                    } else {
                        currentOnCallback(callback)
                    }
                },
            )
        }

    AndroidView(
        modifier = modifier.fillMaxWidth().height(height),
        factory = { context ->
            hostWebView(
                context = context,
                startUrl = BRIDGE_URL,
                onMessage = session::onMessage,
                bundlesDir = File(context.filesDir, WEB_PLUGINS_DIR),
            )
        },
        update = { session.updateProps(props) },
        onRelease = { it.destroy() },
    )
}
