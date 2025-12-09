# DHIS2 Android Plugin Architecture

## Overview

This document describes the recommended architecture for running DHIS2 web plugins inside the Android app using a local HTTP server approach. This solution enables **any DHIS2 plugin** to work without modification.

## Problem Statement

DHIS2 plugins are designed for web-to-web iframe communication using the `post-robot` library. They expect:

1. To run inside an iframe
2. A parent window that responds to post-robot messages
3. HTTP/HTTPS origin (not `file://`)
4. Access to DHIS2 API endpoints

When loading plugins directly in an Android WebView with `file://` URLs:
- `window.parent === window` (no iframe hierarchy)
- Cross-origin restrictions prevent iframe communication
- `fetch()` calls to `/api/*` fail
- Post-robot communication breaks

## Solution: Local HTTP Server

Run an embedded HTTP server inside the Android app that:
- Serves plugin files via `http://localhost:PORT/`
- Acts as a proxy for `/api/*` calls → routes to DHIS2 SDK
- Hosts a bridge page that handles post-robot communication
- Provides proper HTTP origin for iframe communication

```
┌─────────────────────────────────────────────────────────────┐
│  Android App                                                │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  PluginServer (NanoHTTPD)                             │  │
│  │  http://localhost:8080                                │  │
│  │                                                       │  │
│  │  Routes:                                              │  │
│  │  ├── /                    → bridge.html (parent)      │  │
│  │  ├── /plugin/*            → plugin assets             │  │
│  │  ├── /api/system/info     → SDK: d2.systemInfoModule()│  │
│  │  ├── /api/me              → SDK: d2.userModule()      │  │
│  │  ├── /api/dataElements/*  → SDK: d2.dataElementModule │  │
│  │  └── /api/*               → SDK (generic routing)     │  │
│  └───────────────────────────────────────────────────────┘  │
│                         ↑                                   │
│                         │ HTTP                              │
│                         ↓                                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  WebView                                              │  │
│  │  loads http://localhost:8080/                         │  │
│  │                                                       │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │ bridge.html (parent window)                     │  │  │
│  │  │                                                 │  │  │
│  │  │  - Listens for post-robot messages              │  │  │
│  │  │  - Provides props to plugin via post-robot      │  │  │
│  │  │  - Forwards setFieldValue to Android            │  │  │
│  │  │                                                 │  │  │
│  │  │  ┌───────────────────────────────────────────┐  │  │  │
│  │  │  │ <iframe src="/plugin/plugin.html">       │  │  │  │
│  │  │  │                                          │  │  │  │
│  │  │  │  DHIS2 Plugin (unmodified)               │  │  │  │
│  │  │  │  - post-robot works ✓                    │  │  │  │
│  │  │  │  - fetch("/api/*") works ✓               │  │  │  │
│  │  │  │  - Same origin ✓                         │  │  │  │
│  │  │  │                                          │  │  │  │
│  │  │  └───────────────────────────────────────────┘  │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
│                         ↑                                   │
│                         │ JavascriptInterface               │
│                         ↓                                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  PluginBridge (Kotlin)                                │  │
│  │                                                       │  │
│  │  - Receives setFieldValue callbacks from JS           │  │
│  │  - Sends updated props to WebView                     │  │
│  │  - Coordinates with Form/ViewModel                    │  │
│  └───────────────────────────────────────────────────────┘  │
│                         ↑                                   │
│                         │                                   │
│                         ↓                                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  DHIS2 Android SDK                                    │  │
│  │                                                       │  │
│  │  d2.trackedEntityModule()                             │  │
│  │  d2.eventModule()                                     │  │
│  │  d2.dataElementModule()                               │  │
│  │  etc.                                                 │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Implementation Components

### 1. PluginServer.kt

Embedded HTTP server using NanoHTTPD that serves plugin assets and proxies API calls.

```kotlin
// Location: form/src/main/java/org/dhis2/form/ui/plugin/server/PluginServer.kt

class PluginServer(
    private val context: Context,
    private val d2: D2,
    private val port: Int = 8080
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        
        return when {
            // Serve bridge.html for root
            uri == "/" || uri == "/index.html" -> serveBridgeHtml()
            
            // Serve plugin assets
            uri.startsWith("/plugin/") -> servePluginAsset(uri)
            
            // Proxy API calls to SDK
            uri.startsWith("/api/") -> handleApiCall(session)
            
            else -> newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not found")
        }
    }
    
    private fun handleApiCall(session: IHTTPSession): Response {
        val uri = session.uri
        
        return when {
            uri.contains("/api/system/info") -> {
                val info = d2.systemInfoModule().systemInfo().blockingGet()
                jsonResponse(info)
            }
            uri.contains("/api/me") -> {
                val user = d2.userModule().user().blockingGet()
                jsonResponse(user)
            }
            // Add more API mappings as needed
            else -> jsonResponse(mapOf<String, Any>())
        }
    }
}
```

### 2. bridge.html

Parent page that hosts the plugin iframe and handles post-robot communication.

```html
<!-- Location: form/src/main/assets/plugin-bridge/bridge.html -->

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Plugin Bridge</title>
    <style>
        html, body { margin: 0; padding: 0; width: 100%; height: 100%; }
        iframe { width: 100%; height: 100%; border: none; }
    </style>
</head>
<body>
    <iframe id="plugin-frame"></iframe>
    
    <script>
        // Plugin configuration passed from Android
        let pluginConfig = {
            pluginPath: '/plugin/plugin.html',
            props: {}
        };
        
        // Receive configuration from Android
        window.setPluginConfig = function(config) {
            pluginConfig = config;
            loadPlugin();
        };
        
        // Load plugin iframe
        function loadPlugin() {
            document.getElementById('plugin-frame').src = pluginConfig.pluginPath;
        }
        
        // Handle post-robot messages from plugin
        window.addEventListener('message', function(event) {
            const data = event.data;
            if (!data || data.type !== 'postrobot_message_request') return;
            
            if (data.name === 'postrobot_hello') {
                sendPostRobotResponse(event.source, data, {
                    instanceID: 'android-bridge-' + Date.now()
                });
            }
            else if (data.name === 'getPropsFromParent') {
                sendPostRobotResponse(event.source, data, {
                    data: buildPropsWithCallbacks()
                });
            }
        });
        
        function buildPropsWithCallbacks() {
            return {
                ...pluginConfig.props,
                setFieldValue: function(params) {
                    // Forward to Android
                    if (window.Android) {
                        window.Android.onSetFieldValue(JSON.stringify(params));
                    }
                },
                setContextFieldValue: function(params) {
                    if (window.Android) {
                        window.Android.onSetContextFieldValue(JSON.stringify(params));
                    }
                }
            };
        }
        
        function sendPostRobotResponse(target, request, responseData) {
            // Send ACK
            target.postMessage({
                type: 'postrobot_message_ack',
                hash: request.hash,
                name: request.name
            }, '*');
            
            // Send response
            target.postMessage({
                type: 'postrobot_message_response',
                hash: request.hash,
                name: request.name,
                data: responseData
            }, '*');
        }
        
        // Receive prop updates from Android
        window.updateProps = function(props) {
            pluginConfig.props = props;
            // Send updated props to plugin via post-robot 'updated' event
            const iframe = document.getElementById('plugin-frame');
            if (iframe.contentWindow) {
                iframe.contentWindow.postMessage({
                    type: 'postrobot_message_request',
                    name: 'updated',
                    hash: 'update-' + Date.now(),
                    data: { data: buildPropsWithCallbacks() },
                    fireAndForget: true
                }, '*');
            }
        };
    </script>
</body>
</html>
```

### 3. PluginBridge.kt

Kotlin interface between WebView and the rest of the Android app.

```kotlin
// Location: form/src/main/java/org/dhis2/form/ui/plugin/PluginBridge.kt

class PluginBridge(
    private val onSetFieldValue: (SetFieldValueParams) -> Unit,
    private val onSetContextFieldValue: (SetContextFieldValueParams) -> Unit
) {
    
    @JavascriptInterface
    fun onSetFieldValue(paramsJson: String) {
        val params = Gson().fromJson(paramsJson, SetFieldValueParams::class.java)
        onSetFieldValue(params)
    }
    
    @JavascriptInterface
    fun onSetContextFieldValue(paramsJson: String) {
        val params = Gson().fromJson(paramsJson, SetContextFieldValueParams::class.java)
        onSetContextFieldValue(params)
    }
}

data class SetFieldValueParams(
    val fieldId: String,
    val value: Any?,
    val options: FieldValueOptions? = null
)

data class SetContextFieldValueParams(
    val fieldId: String, // "geometry" | "occurredAt" | "enrolledAt"
    val value: Any?,
    val options: FieldValueOptions? = null
)

data class FieldValueOptions(
    val valid: Boolean? = null,
    val touched: Boolean? = null,
    val error: String? = null
)
```

### 4. PluginWebView Composable

Compose component that manages the WebView and server lifecycle.

```kotlin
// Location: form/src/main/java/org/dhis2/form/ui/plugin/PluginWebView.kt

@Composable
fun PluginWebView(
    pluginId: String,
    props: PluginProps,
    onSetFieldValue: (SetFieldValueParams) -> Unit,
    onSetContextFieldValue: (SetContextFieldValueParams) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val d2 = // inject D2 instance
    
    // Remember server instance
    val server = remember {
        PluginServer(context, d2).also { it.start() }
    }
    
    // Cleanup server on dispose
    DisposableEffect(Unit) {
        onDispose { server.stop() }
    }
    
    // WebView
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                
                addJavascriptInterface(
                    PluginBridge(onSetFieldValue, onSetContextFieldValue),
                    "Android"
                )
                
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // Send initial configuration
                        val config = mapOf(
                            "pluginPath" to "/plugin/$pluginId/plugin.html",
                            "props" to props
                        )
                        val configJson = Gson().toJson(config)
                        view?.evaluateJavascript(
                            "window.setPluginConfig($configJson)",
                            null
                        )
                    }
                }
                
                loadUrl("http://localhost:${server.listeningPort}/")
            }
        },
        update = { webView ->
            // Update props when they change
            val propsJson = Gson().toJson(props)
            webView.evaluateJavascript("window.updateProps($propsJson)", null)
        }
    )
}
```

### 5. Plugin Registration

System for registering and managing available plugins.

```kotlin
// Location: form/src/main/java/org/dhis2/form/ui/plugin/PluginRegistry.kt

object PluginRegistry {
    
    private val plugins = mutableMapOf<String, PluginDefinition>()
    
    fun register(plugin: PluginDefinition) {
        plugins[plugin.id] = plugin
    }
    
    fun getPlugin(id: String): PluginDefinition? = plugins[id]
    
    fun getPluginsForDataElement(dataElementId: String): List<PluginDefinition> {
        // Logic to determine which plugins apply to a data element
        return plugins.values.filter { it.appliesTo(dataElementId) }
    }
}

data class PluginDefinition(
    val id: String,
    val name: String,
    val version: String,
    val assetPath: String, // Path in assets folder
    val supportedValueTypes: List<ValueType> = emptyList(),
    val appliesTo: (dataElementId: String) -> Boolean = { false }
)
```

## Data Flow

### 1. Plugin Initialization

```
┌──────────┐    ┌─────────────┐    ┌───────────┐    ┌────────┐
│  Form    │───>│ PluginServer│───>│  WebView  │───>│ Plugin │
│ViewModel │    │   start()   │    │  loadUrl  │    │  loads │
└──────────┘    └─────────────┘    └───────────┘    └────────┘
     │                                                   │
     │           setPluginConfig(props)                  │
     └──────────────────────────────────────────────────>│
                                                         │
                       getPropsFromParent                │
     <───────────────────────────────────────────────────┤
     │                                                   │
     │           props with callbacks                    │
     └──────────────────────────────────────────────────>│
                                                         │
                      Plugin renders ✓                   │
```

### 2. User Interaction (setFieldValue)

```
┌────────┐    ┌────────────┐    ┌──────────────┐    ┌──────────┐
│ Plugin │───>│ bridge.html│───>│PluginBridge │───>│ViewModel │
│ button │    │ postMessage│    │ @JSInterface│    │ saveValue│
└────────┘    └────────────┘    └──────────────┘    └──────────┘
     │                                                    │
     │  setFieldValue({fieldId, value})                   │
     └───────────────────────────────────────────────────>│
                                                          │
                                                   SDK save
                                                          │
     <────────────────────────────────────────────────────┤
     │              props updated                         │
     │<───────────────────────────────────────────────────┘
```

### 3. API Call (fetch)

```
┌────────┐    ┌─────────────┐    ┌─────────┐
│ Plugin │───>│PluginServer │───>│  SDK    │
│ fetch  │    │ /api/*      │    │ module  │
└────────┘    └─────────────┘    └─────────┘
     │                                │
     │  fetch("/api/dataElements")    │
     └───────────────────────────────>│
                                      │
              d2.dataElementModule()  │
                    .get()            │
                                      │
     <────────────────────────────────┤
     │         JSON response          │
```

## File Structure

```
form/
├── src/main/
│   ├── assets/
│   │   ├── plugin-bridge/
│   │   │   └── bridge.html           # Parent bridge page
│   │   └── plugins/
│   │       └── simple-form-field/    # Example plugin
│   │           ├── plugin.html
│   │           └── assets/
│   │               ├── plugin.js
│   │               └── plugin.css
│   │
│   └── java/org/dhis2/form/ui/plugin/
│       ├── server/
│       │   ├── PluginServer.kt       # NanoHTTPD server
│       │   └── ApiHandler.kt         # SDK API routing
│       ├── PluginBridge.kt           # JS interface
│       ├── PluginWebView.kt          # Compose component
│       ├── PluginProps.kt            # Data classes
│       ├── PluginRegistry.kt         # Plugin management
│       └── PluginInterface.kt        # Existing interface
```

## Dependencies

Add to `form/build.gradle.kts`:

```kotlin
dependencies {
    // NanoHTTPD for embedded HTTP server
    implementation("org.nanohttpd:nanohttpd:2.3.1")
}
```

## Security Considerations

1. **Server binding**: Bind only to `localhost` (127.0.0.1), not all interfaces
2. **Port selection**: Use dynamic port allocation to avoid conflicts
3. **Request validation**: Validate all incoming requests
4. **SDK access**: Only expose necessary SDK methods via API proxy

## Benefits of This Approach

| Aspect | Benefit |
|--------|---------|
| **Compatibility** | Works with ANY unmodified DHIS2 plugin |
| **Same-origin** | HTTP origin enables proper iframe communication |
| **API Interception** | Server-side routing to SDK for offline support |
| **Generic** | One implementation supports all plugins |
| **Testable** | Server can be tested independently |
| **Scalable** | Easy to add more plugins and API endpoints |

## Future Enhancements

1. **Plugin marketplace**: Download plugins from DHIS2 app hub
2. **Hot reload**: Update plugins without app restart
3. **Plugin sandboxing**: Isolate plugin permissions
4. **Caching**: Cache plugin assets and API responses
5. **Offline queue**: Queue API writes when offline

## References

- [NanoHTTPD GitHub](https://github.com/NanoHttpd/nanohttpd)
- [DHIS2 Plugin Documentation](https://developers.dhis2.org/docs/app-platform/plugins)
- [Post-Robot Library](https://github.com/krakenjs/post-robot)
- [WebView Best Practices](https://developer.android.com/develop/ui/views/layout/webapps/webview)
