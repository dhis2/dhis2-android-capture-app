# DHIS2 Android Plugin Architecture — Web Plugins (HTML Views)

> **Scope:** this document covers the *web plugin* track (HTML Views PoC).
> Phases A + B are implemented on this branch; Phases C + D are documented at
> the end for context but live on a follow-up branch.

## Overview

This document describes how the Android Capture App runs **unmodified** DHIS2
web plugins — the same React + `@dhis2/app-runtime` bundles the web Capture
app loads through the Tracker Plugin Configurator. The goal is that installing
or swapping a plugin is a server-side configuration act only: no Android code
change, no new APK.

## Problem statement

DHIS2 web plugins are designed for web-to-web iframe communication using
[`post-robot`](https://github.com/krakenjs/post-robot). They expect:

1. To run inside an iframe whose parent is a real web origin.
2. A parent window that speaks post-robot (the protocol `@dhis2/app-runtime`'s
   Plugin hook drives).
3. HTTP/HTTPS origin — not `file://`.
4. Access to the DHIS2 API at `${baseUrl}/api/*`.

Loading the plugin directly in a WebView via `file://` fails all four:
`window.parent === window`, cross-origin checks block postMessage, `fetch`
resolves against `file:///`, service workers don't register.

## Solution: `WebViewAssetLoader` + generic bridge + SDK-backed `/api/*` proxy

We give the WebView a synthetic HTTPS origin (`https://appassets.androidplatform.net`)
using AndroidX [`WebViewAssetLoader`](https://developer.android.com/reference/androidx/webkit/WebViewAssetLoader).
No real server, no port, no socket — the loader intercepts `shouldInterceptRequest`
and maps URL paths to local content or Kotlin-backed responses.

Three `PathHandler`s are registered:

- `/host/` → ships the generic `bridge.html` + vendored `post-robot.min.js`
  from `form/src/main/assets/dhis2-plugin-host/`.
- `/plugins/` → ships the plugin bundle(s). In Phase A + B this reads from
  APK assets (`form/src/main/assets/plugins/`). Phase C swaps this for
  `{filesDir}/plugins/` so bundles are downloaded at runtime.
- `/api/` → routed to `Dhis2SdkApiDispatcher`, which translates into D2 SDK
  calls and returns JSON. Offline-first for free: the SDK already owns the
  local DB.

### Why `WebViewAssetLoader` rather than a local HTTP server

We briefly explored embedding NanoHTTPD on `127.0.0.1:PORT`. Both give the
plugin a real origin, but `WebViewAssetLoader` is strictly simpler:

- No TCP socket, no port management, no lifecycle to supervise.
- No Play-Store policy friction (apps running local servers get flagged).
- Service workers work without secure-context workarounds.
- Synchronous `PathHandler.handle()` fits the D2 SDK's `blockingGet()` API
  and the WebView network thread.

The only NanoHTTPD advantage — asynchronous request handling inside the
server — is not needed here: the handler runs off the UI thread and the SDK
already exposes blocking getters used widely in the Capture App.

## Architecture

```
┌──────────────────────────── Android WebView (Capture App) ──────────────────────────┐
│  Loads https://appassets.androidplatform.net/host/bridge.html                       │
│                                                                                     │
│  ┌──────────────────────────────── bridge.html ──────────────────────────────────┐  │
│  │  • post-robot parent:                                                         │  │
│  │      postRobot.on("getPropsFromParent", …) → replies with props + callbacks   │  │
│  │      postRobot.send(plugin, "updated", props) on host state change            │  │
│  │  • Exposed Kotlin bridge (via @JavascriptInterface Android):                  │  │
│  │      setFieldValue        → window.Android.onSetFieldValue(JSON.stringify(p)) │  │
│  │      setContextFieldValue → window.Android.onSetContextFieldValue(…)          │  │
│  │                                                                               │  │
│  │  <iframe src="https://appassets.androidplatform.net/plugins/{id}-{ver}/…">    │  │
│  │     Unmodified DHIS2 plugin (same origin → post-robot works)                  │  │
│  │  </iframe>                                                                    │  │
│  └───────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                     │
│  WebViewAssetLoader                                                                 │
│   ├─ /host/    → AssetsPathHandler("dhis2-plugin-host")                             │
│   │              bridge.html, post-robot.min.js (shipped with app)                  │
│   ├─ /plugins/ → AssetsPathHandler("plugins")                                       │
│   │              {id}-{version}/plugin.html + assets (APK today, filesDir in C)     │
│   └─ /api/     → Dhis2SdkApiPathHandler                                             │
│                  /api/system/info, /api/me, /api/userSettings, /api/dataStore/…     │
│                  → D2 SDK (blockingGet), JSON response                              │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

## Implementation components

### `bridge.html` (`form/src/main/assets/dhis2-plugin-host/bridge.html`)

Generic, plugin-agnostic parent page. Loads post-robot, implements the parent
half of the plugin protocol, and exposes two entry points called from Kotlin
via `evaluateJavascript`:

- `window.configurePlugin({ pluginUrl, props })` — boots the plugin iframe.
- `window.updateProps(jsonString)` — pushes a fresh prop set to the plugin
  over post-robot's `updated` event.

### Vendored post-robot (`form/src/main/assets/dhis2-plugin-host/post-robot.min.js`)

Version **10.0.46**, matching the copy bundled by `@dhis2/app-runtime@3.x`
inside every built DHIS2 plugin. See
`form/src/main/assets/dhis2-plugin-host/README.md` for install instructions
and bump policy.

### `PluginWebViewHost` (Composable)

`form/src/main/java/org/dhis2/form/ui/plugin/PluginWebViewHost.kt`.
Owns the WebView, builds the `WebViewAssetLoader`, wires
`shouldInterceptRequest` + `onPageFinished`, and exposes a simple Compose API:

```kotlin
PluginWebViewHost(
    pluginId = "simple-capture-plugin",
    pluginVersion = "1.0.0",
    props = pluginProps,
    onSetFieldValue = { params -> /* → FormIntent.OnSave in Phase D */ },
    onSetContextFieldValue = { params -> /* → dedicated intents in Phase D */ },
)
```

### `PluginBridge` (`@JavascriptInterface`)

`form/src/main/java/org/dhis2/form/ui/plugin/PluginBridge.kt`. Two entry
points: `onSetFieldValue(String)` and `onSetContextFieldValue(String)`,
each deserialising a `SetFieldValueParams` / `SetContextFieldValueParams`
DTO via Gson.

### `Dhis2SdkApiDispatcher` + `Dhis2SdkApiPathHandler`

`form/src/main/java/org/dhis2/form/ui/plugin/api/Dhis2SdkApiDispatcher.kt`
and `path/Dhis2SdkApiPathHandler.kt`. Phase B scope:

| Path | SDK call |
| --- | --- |
| `/api/system/info` | `d2.systemInfoModule().systemInfo().blockingGet()` |
| `/api/me` | `d2.userModule().user().blockingGet()` |
| `/api/userSettings` | `{}` (passthrough) |

Server-side `/api/dataStore/{ns}/{key}` routing is deferred until real plugins
start hitting it — the SDK's dataStoreDownload API needs verification and is
best designed once we know which namespaces matter. Phase D lands with the
download pipeline.

**API version prefix.** `@dhis2/app-runtime` reads the server version from
`/api/system/info` and then prefixes subsequent calls with it — e.g.
`/api/43/me`, `/api/43/userSettings`. `Dhis2SdkApiDispatcher` strips a
leading numeric segment before matching, so a single handler covers both
versioned and unversioned paths.

**`/api/me` shape.** `@dhis2/app-runtime`'s `DataProvider` blocks its children
until `/api/me` resolves to a valid user. An empty `{}` is not enough — the
dispatcher returns at minimum `{ id, username, authorities[], userCredentials }`.
`authorities` comes from `d2.userModule().authorities().blockingGet()`.

Unknown GETs log a WARN and return `200 {}` so plugins degrade gracefully
instead of erroring. Writes (POST/PUT/DELETE) return `405` — plugins that
need writebacks should use the `setFieldValue` callback, not `/api/*`.

## Data flow

### Initialisation

```
Kotlin → WebView: loadUrl("https://.../host/bridge.html")
bridge.html → post-robot listener armed
Kotlin → WebView: evaluateJavascript("window.configurePlugin({pluginUrl, props})")
bridge.html → iframe.src = pluginUrl
Plugin → postRobot.send(parent, "getPropsFromParent") → parent returns props
Plugin renders ✓
```

### `setFieldValue`

```
Plugin → props.setFieldValue({fieldId, value})
bridge.html → post-robot proxy round-trip
bridge.html → window.Android.onSetFieldValue(JSON)
PluginBridge.kt → onSetFieldValue callback → (Phase D) FormIntent.OnSave
```

### `/api/*` call

```
Plugin → fetch("/api/system/info")
WebView → shouldInterceptRequest → WebViewAssetLoader → Dhis2SdkApiPathHandler
Dhis2SdkApiDispatcher → d2.systemInfoModule().systemInfo().blockingGet()
Dhis2SdkApiPathHandler → WebResourceResponse(application/json, 200, body)
Plugin receives real data (offline-first via SDK's local DB)
```

## File structure

```
form/
├── src/main/
│   ├── assets/
│   │   ├── dhis2-plugin-host/               # ships with app, one generic bridge
│   │   │   ├── bridge.html
│   │   │   ├── post-robot.min.js            # vendored 10.0.46 (see README)
│   │   │   └── README.md
│   │   └── plugins/                         # Phase A+B: plugins in APK (black box)
│   │       └── simple-capture-plugin-1.0.0/ # Phase C: move to {filesDir}/plugins/
│   │
│   └── java/org/dhis2/form/ui/plugin/
│       ├── PluginBridge.kt                  # @JavascriptInterface Android
│       ├── PluginWebViewHost.kt             # Composable entry point
│       ├── PluginProps.kt                   # props/metadata data classes
│       ├── PluginDemo.kt                    # dev switch + mock props (delete in Phase D)
│       ├── SetFieldValueParams.kt           # DTOs for callbacks
│       ├── api/
│       │   └── Dhis2SdkApiDispatcher.kt     # path → SDK routing
│       └── path/
│           └── Dhis2SdkApiPathHandler.kt    # WebViewAssetLoader.PathHandler
```

## Dependencies

In `gradle/libs.versions.toml`:

```toml
[versions]
androidxWebkit = "1.12.1"

[libraries]
androidx-webkit = { group = "androidx.webkit", name = "webkit", version.ref = "androidxWebkit" }
```

In `form/build.gradle.kts`:

```kotlin
implementation(libs.androidx.webkit)
```

No NanoHTTPD. No custom HTTP server dependency.

## Security considerations

- **Origin isolation.** The WebView runs on a synthetic HTTPS origin dedicated
  to this loader. Plugins cannot reach the surrounding Android UI except
  through the narrow `@JavascriptInterface` surface defined in `PluginBridge`.
- **Asset integrity.** Phase C adds SHA-256 verification of downloaded plugin
  bundles (config-pinned). Phase A+B run vendored bundles that ship inside
  the APK and are treated as black boxes.
- **SDK scope.** The API dispatcher exposes read-only DHIS2 data. No raw `D2`
  is handed to the plugin. Writes flow through `setFieldValue`, which the
  host validates and applies to its own form model.
- **Signing.** Unlike native plugin bundles (signed jarsigner APKs), DHIS2
  web apps aren't signed by App Hub — we rely on content hashing at
  download time plus per-instance admin curation via dataStore.

## Phase status

| Phase | State | What it adds |
| --- | --- | --- |
| A | ✅ **Done, verified** | Transport swap: `WebViewAssetLoader`, generic post-robot bridge, plugin bundle as APK-asset black box. Bundled `simple-form-field-plugin` renders end-to-end; `getPropsFromParent` + `setFieldValue` round-trip through post-robot → `PluginBridge`. |
| B | ✅ **Done, verified** | `/api/*` → SDK proxy (`Dhis2SdkApiDispatcher`) covers `systemInfo` / `me` / `userSettings` with version-prefix stripping. `@dhis2/app-runtime`'s `DataProvider` boots cleanly against the offline SDK. |
| C | 🚧 **In progress** | Real-field dispatch + write-back. `Form.kt` derives `PluginProps` from its `sections` parameter (real DHIS2 UIDs) and wires `setFieldValue` → `FormIntent.OnSave(uid, value, valueType)`. Also: dynamic height via post-robot `resize` so the WebView container grows/shrinks to match the plugin's natural content size. |
| D | Deferred | Runtime download + dataStore-driven discovery + SHA-256 verification. Server-side config becomes the only install surface; APK stops bundling plugin assets. Deprioritised on 2026-04-23 — tackle only after C lands and the team decides the HTML-views track is worth productising. |

## Lessons learned (from Phase A + B verification)

Four non-obvious gotchas we hit wiring up the unmodified `simple-form-field-plugin`.
Worth flagging so anyone reviewing or extending this knows to expect them.

1. **WebView inside AndroidView needs explicit `LayoutParams`.** Compose's
   `modifier.height(200.dp)` sizes the AndroidView container, but the WebView
   child doesn't automatically inherit `MATCH_PARENT` — its internal viewport
   (`window.innerHeight`) stays at `0`. That makes the plugin's
   `height:100vh` shell collapse to `0` even though the WebView visually
   occupies the full height. Fix:
   ```kotlin
   WebView(ctx).apply {
       layoutParams = ViewGroup.LayoutParams(
           ViewGroup.LayoutParams.MATCH_PARENT,
           ViewGroup.LayoutParams.MATCH_PARENT,
       )
       settings.useWideViewPort = true
       settings.loadWithOverviewMode = true
       // …
   }
   ```

2. **DHIS2 plugins assume the Capture web shell provides `html/body` height.**
   The bundle only sets `#dhis2-app-root { isolation:isolate }` and the
   component uses `height:100vh` at the `.app-shell-adapter` level. Standalone
   in an iframe, `html/body` default to `auto` height and the shell collapses.
   Because bridge.html and the plugin iframe are same-origin
   (`https://appassets.androidplatform.net`), the bridge can inject a
   fix-up stylesheet into the iframe's `<head>` on load:
   ```css
   html, body { height: 100% !important; margin: 0 !important; padding: 0 !important; }
   #dhis2-app-root { display: block; height: 100vh !important; width: 100% !important; }
   ```
   This doesn't touch the plugin bundle — the plugin is still a black box.

3. **`@dhis2/app-runtime`'s `<DataProvider>` blocks on `/api/me`.**
   Returning `{}` isn't enough — it expects a real user object with
   `authorities` and `userCredentials`. Without those, the DataProvider shows
   no UI at all (no error, no spinner, just an empty root). The dispatcher
   returns a populated map sourced from `d2.userModule().user()` +
   `d2.userModule().authorities()`.

4. **post-robot envelope-format drift is real.** The `plugin-architecture.md`
   draft originally hand-rolled `postrobot_message_request` / `_response`
   envelopes. That works for a specific version but is brittle across
   post-robot majors (notably proxy-function serialisation for
   `setFieldValue`-style callbacks). The shipped bridge vendors
   **post-robot 10.0.46** — matching the copy `@dhis2/app-runtime@3.x`
   bundles inside the plugin itself, confirmed by the
   `post_robot_10_0_46__` namespace string inside the built bundle — and
   uses `postRobot.on(...)` / `postRobot.send(...)` instead of mirroring the
   wire format.

## Open questions

- Do we want to share the plugin download/cache/verify layer with the native
  plugin PoC (`feature/plugin-system`)? Decide when planning Phase C.
- Which additional `/api/*` endpoints do real partner plugins need? Driven
  by the WARN log from `Dhis2SdkApiDispatcher.get(path)`.
- Service-worker lifecycle: DHIS2 plugins ship a service worker; need to
  clean up caches when a plugin version is swapped (Phase C).

## References

- [AndroidX `WebViewAssetLoader`](https://developer.android.com/reference/androidx/webkit/WebViewAssetLoader)
- [post-robot](https://github.com/krakenjs/post-robot)
- [DHIS2 app-platform plugins](https://developers.dhis2.org/docs/app-platform/plugins)
- [DHIS2 Android SDK](https://github.com/dhis2/dhis2-android-sdk)
