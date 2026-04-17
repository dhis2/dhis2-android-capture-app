# DHIS2 Android Capture App — Plugin System

> **Status:** preview / `0.1.0-SNAPSHOT`. The public API, distribution format, and
> injection points may still change.

## 1. Overview

The plugin system lets third-party developers extend the DHIS2 Android Capture
App **without forking** it. A plugin is a small Android library that implements
a single interface (`Dhis2Plugin`), is packaged as a DEX file, and is picked up
by the app at login time via a server-side configuration.

It is designed for two audiences:

- **DHIS2 server administrators** — decide which plugins an instance uses, by
  writing a small JSON config into the server dataStore.
- **Third-party Android developers** — build Composable UI and domain logic
  that runs inside the Capture App under a tightly scoped security contract.

The host app does the download, integrity check, sandboxing, DI wiring, and
rendering; the plugin only provides metadata and a Composable.

## 2. Architecture at a glance

```
┌────────────────────────┐   1. build DEX        ┌──────────────────────┐
│ Third-party dev        │──────────────────────▶│ Hosting (App Hub,    │
│ (Android Studio)       │                       │ CDN, local HTTP, …)  │
└────────────────────────┘                       └──────────┬───────────┘
                                                            │ 2. downloadUrl
                                                            ▼
┌────────────────────────┐   3. register JSON   ┌──────────────────────┐
│ DHIS2 server admin     │─────────────────────▶│ DHIS2 server         │
│ (Data Store Manager)   │                       │ dataStore            │
└────────────────────────┘                       │ namespace:           │
                                                 │  dhis2AndroidPlugins │
                                                 │ key: config          │
                                                 └──────────┬───────────┘
                                                            │ 4. fetched at login
                                                            ▼
┌──────────────────────────────────────────────────────────────────────┐
│                       Capture App (host)                              │
│   LoadPluginsUseCase                                                  │
│     → AppHubPluginRepository  (read config)                          │
│     → PluginDownloader        (HTTP GET → filesDir/plugins/*.dex)    │
│     → PluginVerifier          (SHA-256)                              │
│     → PluginLoader            (InMemoryDexClassLoader, API 26+)      │
│     → PluginRegistry          (StateFlow<List<Dhis2Plugin>>)         │
│                                                                      │
│   UI: PluginSlot(InjectionPoint.HOME_ABOVE_PROGRAM_LIST)              │
│     → ScopedDhis2PluginContext (enforces UID allow-lists)            │
│     → plugin.content(context)                                         │
└──────────────────────────────────────────────────────────────────────┘
```

End-to-end flow:

1. The developer implements `Dhis2Plugin`, builds a DEX, hosts it on a URL the
   device can reach.
2. The admin writes a small JSON blob into the DHIS2 dataStore describing the
   plugin (id, version, `downloadUrl`, `checksum`, `allowedProgramUids`,
   `allowedDataSetUids`, `injectionPoints`).
3. On the next successful login (`MainActivity.onCreate`), the app fetches the
   config, downloads each DEX (caching it under
   `filesDir/plugins/{id}-{version}.dex`), verifies its SHA-256, loads it with
   `InMemoryDexClassLoader`, and registers the resulting instance.
4. Any `PluginSlot(injectionPoint = …)` placed in the host UI renders every
   plugin registered for that slot, wrapped in a per-plugin scoped
   `Dhis2PluginContext`.

## 3. The plugin SDK (`:plugin-sdk`)

Maven coordinates (currently published to Maven Local only):

```
org.dhis2.mobile:plugin-sdk:0.1.0-SNAPSHOT
```

The SDK is a Kotlin Multiplatform module (Android + Desktop JVM). External
developers target Android and consume a single artifact.

### 3.1 `Dhis2Plugin`

```kotlin
interface Dhis2Plugin {
    val metadata: PluginMetadata
    fun provideKoinModule(): Module? = null

    @Composable
    fun content(context: Dhis2PluginContext)
}
```

- The implementing class must have a **public no-argument constructor** — the
  host instantiates it via reflection.
- `provideKoinModule()` is optional; if non-null, the module is loaded into the
  host's Koin container at plugin load time.
- `content(context)` is the plugin's UI. It runs **inside the host
  composition** — keep it contained, don't navigate outside the slot.

### 3.2 `PluginMetadata`

```kotlin
@Serializable
data class PluginMetadata(
    val id: String,                               // "org.myorg.my-plugin"
    val version: String,                          // "1.0.0"
    val entryPoint: String,                       // "org.myorg.plugin.MyPlugin"
    val allowedProgramUids: List<String> = emptyList(),
    val allowedDataSetUids: List<String> = emptyList(),
    val injectionPoints: List<InjectionPoint> = emptyList(),
    val downloadUrl: String = "",                 // used only server-side
    val checksum: String = "",                    // "sha256:<hex>"
)
```

The **same data class** is serialized into the server dataStore JSON *and*
returned from the plugin instance at runtime — allow-lists declared in the JSON
are the ones enforced in `Dhis2PluginContext`.

### 3.3 `Dhis2PluginContext`

The **only** gateway through which a plugin may access DHIS2 data. Every
operation is automatically scoped to `allowedProgramUids` /
`allowedDataSetUids` from the plugin's metadata.

```kotlin
interface Dhis2PluginContext {
    val pluginMetadata: PluginMetadata

    // Read
    suspend fun getTrackedEntityInstances(programUid: String):
        Result<List<TrackedEntityInstanceDto>>
    suspend fun getDataValues(orgUnitUid: String, dataSetUid: String, period: String):
        Result<List<DataValueDto>>

    // Write
    suspend fun saveDataValue(dataSetUid: String, dataValue: DataValueDto):
        Result<Unit>
}
```

- Out-of-scope access returns `Result.failure(SecurityException(…))`; it never
  silently returns empty data, and it never throws.
- Returns **DTOs only** (`TrackedEntityInstanceDto`, `DataValueDto`) — raw
  DHIS2 Android SDK (`D2`) types are never exposed to plugin code, so SDK
  evolution does not break existing plugins.
- All blocking `D2` calls are wrapped on `Dispatchers.IO` by the host.

### 3.4 `InjectionPoint`

```kotlin
enum class InjectionPoint {
    HOME_ABOVE_PROGRAM_LIST,
}
```

Today there is only one slot. It is rendered above the program list on the
home screen (see `ProgramUi.kt`).

## 4. The host module (`:plugin`)

Internal module. Developers don't consume it, but the admin-facing behaviour
is worth knowing:

| Component | Responsibility |
|---|---|
| `LoadPluginsUseCase` | Kicked off from `MainActivity.onCreate` on a `lifecycleScope` coroutine. Per-plugin failure-isolated. Skips the whole system on API < 26. |
| `AppHubPluginRepository` | Reads the config JSON from the DHIS2 dataStore (namespace `dhis2AndroidPlugins`, key `config`). Returns an **empty list, not a failure**, if no config is set. |
| `PluginDownloader` | HTTP GET of `downloadUrl` with 10 s connect / 30 s read timeouts. Caches at `filesDir/plugins/{id}-{version}.dex`. Bump `version` to force a re-download. |
| `PluginVerifier` | SHA-256 compare. Format is `sha256:<hex>`. **Blank checksum is accepted with a warning** — handy during local testing, dangerous in production. |
| `PluginLoader` | `InMemoryDexClassLoader` with the host's class loader as parent — so the plugin resolves `:plugin-sdk` types from the host at runtime instead of bundling its own copy. Requires API 26+. |
| `PluginRegistry` | `MutableStateFlow<List<Dhis2Plugin>>`. Queried reactively by `PluginSlot`. |
| `ScopedDhis2PluginContext` | Enforces UID allow-lists; wraps `D2` calls on `Dispatchers.IO`; maps to DTOs. |
| `PluginSlot(injectionPoint)` | Composable. Drop it anywhere in the host UI; it renders every plugin declaring that `InjectionPoint`, each inside a `key(plugin.metadata.id) { … }` block. |

## 5. Server-side configuration

The admin writes a JSON object into the DHIS2 server dataStore at:

- **namespace:** `dhis2AndroidPlugins`
- **key:** `config`

Schema:

```json
{
  "plugins": [
    {
      "id": "org.myorg.my-plugin",
      "version": "1.0.0",
      "entryPoint": "org.myorg.plugin.MyPlugin",
      "downloadUrl": "https://apps.dhis2.org/api/apps/my-plugin/1.0.0/plugin.dex",
      "checksum": "sha256:abc123…",
      "allowedProgramUids": ["UID1"],
      "allowedDataSetUids": [],
      "injectionPoints": ["HOME_ABOVE_PROGRAM_LIST"]
    }
  ]
}
```

Any number of plugins can be declared. Plugins the app cannot download or
verify are skipped silently (see logcat) — the rest still load.

Easiest way to edit this from the server side is the **Data Store Manager**
app on the DHIS2 web. It can also be done via the REST API:

```bash
curl -u <user:pass> -X POST \
  -H "Content-Type: application/json" \
  "https://<server>/api/dataStore/dhis2AndroidPlugins/config" \
  --data @config.json
```

(Use `PUT` on subsequent updates.)

## 6. Writing a plugin — step by step

The walkthrough below mirrors the sample project at
`AndroidStudioProjects/Pluginimplementationtest`. That project has a two-module
layout:

```
Pluginimplementationtest/
├── app/      # com.android.application — runnable harness with MainActivity +
│             #   StubDhis2PluginContext. Used to preview the plugin without
│             #   having to install the Capture App.
└── plugin/   # com.android.library  — the actual plugin code. Produces the
              #   standalone DEX consumed by the Capture App via the
              #   `:plugin:buildPluginDex` Gradle task.
```

The plugin code lives in `:plugin`. The `:app` module is a dev-only harness and
is **not** shipped to end users.

### 6.1 Gradle setup

**`settings.gradle.kts`** — include `mavenLocal()` while the SDK is a SNAPSHOT,
and declare both modules:

```kotlin
pluginManagement {
    repositories {
        google { /* … */ }
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}
include(":app")
include(":plugin")
```

**`plugin/build.gradle.kts`** — the library module. Note every runtime-provided
dep is `compileOnly`:

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.myorg.myplugin"
    compileSdk { version = release(36) }
    defaultConfig { minSdk = 26 }            // required: InMemoryDexClassLoader
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // compileOnly is LOAD-BEARING: these are provided by the Capture App at
    // runtime via InMemoryDexClassLoader's parent delegation. Bundling them
    // causes DEX bloat and ClassCastException from duplicated classes.
    compileOnly("org.dhis2.mobile:plugin-sdk:0.1.0-SNAPSHOT")
    compileOnly(platform(libs.androidx.compose.bom))
    compileOnly(libs.androidx.compose.ui)
    compileOnly(libs.androidx.compose.material3)
}

// See §6.3 — the buildPluginDex task goes here.
```

**`app/build.gradle.kts`** — the harness. Depends on `:plugin` so `MainActivity`
can preview `MyPlugin` without the Capture App:

```kotlin
dependencies {
    implementation(project(":plugin"))
    implementation("org.dhis2.mobile:plugin-sdk:0.1.0-SNAPSHOT") // StubDhis2PluginContext
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}
```

### 6.2 Implement `Dhis2Plugin`

```kotlin
package org.myorg.myplugin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.plugin.sdk.*

class MyPlugin : Dhis2Plugin {
    override val metadata = PluginMetadata(
        id = "org.myorg.myplugin",
        version = "1.0.0",
        entryPoint = "org.myorg.myplugin.MyPlugin",
        injectionPoints = listOf(InjectionPoint.HOME_ABOVE_PROGRAM_LIST),
    )

    override fun provideKoinModule() = null

    @Composable
    override fun content(context: Dhis2PluginContext) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Hello from ${context.pluginMetadata.id}!",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
```

Key rules:

- The class must be **public** and have a **no-arg constructor**.
- `entryPoint` must be the exact fully-qualified class name — this is what
  `classLoader.loadClass(...)` resolves.
- `id` and `version` in the code should match the dataStore JSON; the
  `version` is part of the DEX cache key, so bumping it forces a re-download.

### 6.3 Build the DEX

The sample's `plugin/build.gradle.kts` defines a `buildPluginDex` task that
produces a single standalone DEX. It (1) builds the release AAR, (2) extracts
`classes.jar`, (3) runs `d8` on it, (4) names the output
`{pluginId}-{pluginVersion}.dex`, (5) prints size + SHA-256.

```bash
./gradlew :plugin:buildPluginDex
```

Output:

```
Built plugin DEX
  path:     plugin/build/outputs/plugin-dex/org.myorg.myplugin-1.0.0.dex
  size:     ~16 KB
  checksum: sha256:…
```

The DEX contains **only** the plugin's own compiled classes. All Compose,
Material3, AndroidX, and `plugin-sdk` references are unresolved in the DEX —
they'll be resolved from the host's class loader at runtime.

The task body is short (~60 lines) and lives with the plugin rather than
shipped as a reusable Gradle plugin yet. Copy it into your own project's
`plugin/build.gradle.kts`. It uses the `d8` binary from
`{sdk}/build-tools/<ver>/d8`, resolving the SDK from `ANDROID_HOME`,
`ANDROID_SDK_ROOT`, or `local.properties`.

### 6.4 Host the DEX and register it

Upload `plugin/build/outputs/plugin-dex/{id}-{version}.dex` to a URL the
device can reach, take the SHA-256 printed by the task, and add an entry to
the dataStore JSON (section 5). That's it.

### 6.5 Preview the plugin locally (optional)

The `:app` harness lets you see the plugin render without installing the
Capture App:

```bash
./gradlew :app:installDebug
```

`MainActivity` instantiates `MyPlugin`, wires it to `StubDhis2PluginContext`
(returns empty data, success on writes), and renders
`MyPlugin.content(...)` inside a mock "program list" scaffold.

## 7. Security model

- **Scope enforcement.** Every read/write from `Dhis2PluginContext` is checked
  against `allowedProgramUids` / `allowedDataSetUids`. Violations surface as
  `Result.failure(SecurityException)` to the plugin and an `e`-level log on
  the host.
- **DTO-only boundary.** Plugins never see `D2` or its entity types. This
  insulates plugins from SDK evolution and prevents them from escaping the
  sandbox via the SDK's fluent API.
- **Integrity.** SHA-256 is verified before the DEX is loaded. A mismatch
  evicts the cache and skips the plugin.
- **API guard.** `InMemoryDexClassLoader` requires API 26+; on older devices
  the whole system is skipped.
- **Process.** Plugins run **in-process** with the host. A crash inside a
  plugin propagates to the enclosing composition — pick trusted authors.

## 8. Current limitations

- One injection point only: `HOME_ABOVE_PROGRAM_LIST`.
- No published Gradle plugin yet — each plugin project copies the
  `buildPluginDex` task (~60 lines) into its own `plugin/build.gradle.kts`.
- No plugin uninstall flow (clear `filesDir/plugins/` manually or remove the
  dataStore entry; the next login will stop loading it).
- No code-signing / author signature verification — only a hash.
- Plugins share the host's `D2` session and Koin graph; misbehaving Koin
  bindings in a plugin module can affect the host.
- `Dhis2PluginContext` currently exposes only TEIs and data values. Events,
  enrollments, and org-unit resolution are not yet on the API surface.
- No `plugin-sdk-test` artefact yet — plugin authors copy-paste their own
  `StubDhis2PluginContext` for previews. A future release could ship a
  fluent `FakeDhis2PluginContext` with configurable fixtures.

## 9. Testing a plugin locally

This walkthrough targets the **Android emulator** + a **local Python HTTP
server** + the sample project at
`/Users/andresmr/AndroidStudioProjects/Pluginimplementationtest`.

### Step 1 — Publish the SDK to Maven Local

From the Capture App repo:

```bash
cd ~/StudioProjects/ai-dhis2-android-capture-app
./gradlew :plugin-sdk:publishToMavenLocal
```

Verify:

```bash
ls ~/.m2/repository/org/dhis2/mobile/plugin-sdk/0.1.0-SNAPSHOT/
```

You should see an `.aar` and a `.module` file.

### Step 2 — Build the plugin DEX

```bash
cd ~/AndroidStudioProjects/Pluginimplementationtest
./gradlew :plugin:buildPluginDex
```

The task prints the output path, size, and SHA-256. The produced file lives at:

```
plugin/build/outputs/plugin-dex/org.dhis2.myplugin-1.0.0.dex
```

Record the SHA-256 — you'll paste it into the dataStore entry in step 4 (or
leave `checksum` empty and let `PluginVerifier` skip it with a warning).

> **Why `buildPluginDex` and not `assembleDebug`?** An app build produces
> multi-dex APKs (`classes.dex`, `classes2.dex`, …) whose `classes.dex` is
> not the plugin — it's Compose/Material bloat. `buildPluginDex` runs `d8`
> on just the `:plugin` library's own `classes.jar`, yielding a ~16 KB DEX
> that contains the plugin's own classes only. The rest (Compose, Material3,
> `plugin-sdk`) is provided by the host's class loader at runtime.

### Step 3 — Serve the DEX to the emulator

```bash
cd ~/AndroidStudioProjects/Pluginimplementationtest/plugin/build/outputs/plugin-dex
python3 -m http.server 8080
```

From inside the emulator, `10.0.2.2` points back at your host machine. So
the URL the app will use is:

```
http://10.0.2.2:8080/org.dhis2.myplugin-1.0.0.dex
```

(The filename is `{pluginId}-{pluginVersion}.dex` — `org.dhis2.myplugin` is the
sample plugin's id, not a path prefix.)

(For a physical device on the same LAN, use the host's LAN IP instead of
`10.0.2.2`. Cleartext HTTP to a LAN IP works on debug builds because
`usesCleartextTraffic` is typically enabled there; if it isn't, add the IP to
`network_security_config.xml`.)

### Step 4 — Register the plugin in the server dataStore

Pick a DHIS2 dev server you can log into from the app and whose dataStore you
can write to (e.g. `https://play.dhis2.org/…` with admin, or your local
docker instance). From the DHIS2 web, open **Data Store Manager** → add key
**`config`** under namespace **`dhis2AndroidPlugins`** with:

```json
{
  "plugins": [
    {
      "id": "org.dhis2.myplugin",
      "version": "1.0.0",
      "entryPoint": "org.dhis2.pluginimplementationtest.MyPlugin",
      "downloadUrl": "http://10.0.2.2:8080/org.dhis2.myplugin-1.0.0.dex",
      "checksum": "sha256:<paste hash from step 2>",
      "allowedProgramUids": [],
      "allowedDataSetUids": [],
      "injectionPoints": ["HOME_ABOVE_PROGRAM_LIST"]
    }
  ]
}
```

Or via curl:

```bash
curl -u admin:district -X POST \
  -H "Content-Type: application/json" \
  "https://<server>/api/dataStore/dhis2AndroidPlugins/config" \
  -d '{"plugins":[{ … as above … }]}'
```

For a first smoke test you can set `"checksum": ""` — `PluginVerifier` logs a
warning and skips verification.

### Step 5 — Run the Capture App and verify

1. Run the `dhis2Debug` variant on the emulator and log into the server you
   just configured.
2. On the home screen, above the program list, the sample plugin's green
   card ("Hello from MyPlugin! 👋") should appear.

Watch the logs while this happens:

```bash
adb logcat | grep -E "Plugin|Dhis2Plugin"
```

You should see, in order:

```
Found 1 plugin(s) in server configuration
Downloading plugin 'org.dhis2.myplugin' v1.0.0 from http://10.0.2.2:8080/…
Plugin cached to /data/data/com.dhis2.debug/files/plugins/org.dhis2.myplugin-1.0.0.dex
Loading plugin 'org.dhis2.myplugin' v1.0.0 from DEX (16404 bytes)
Plugin 'org.dhis2.myplugin' v1.0.0 loaded successfully
```

The tell-tale sign the right file is being served: size ~16 KB, not ~17 MB.

### Step 6 — Iterate

Each time you change the plugin:

1. Rebuild the DEX: `./gradlew :plugin:buildPluginDex`.
2. Either **bump `pluginVersion`** in `plugin/build.gradle.kts` (and also in
   the dataStore JSON or hardcoded fallback), **or** clear the on-device
   cache manually:

   ```bash
   adb shell run-as com.dhis2.debug rm -rf files/plugins
   ```

   (Replace `com.dhis2.debug` with the applicationId of whichever variant you
   are running — `com.dhis2` for release, `com.dhis2.debug` for `dhis2Debug`,
   etc.)

3. Restart the app and log in again.

> **Note.** While iterating locally you can short-circuit the server-side
> dataStore step entirely: `plugin/src/main/java/org/dhis2/mobile/plugin/data/AppHubPluginRepository.kt`
> contains a `FALLBACK_CONFIG_JSON` constant that the host falls back to when
> no dataStore entry exists. Update its `checksum` / `version` / `downloadUrl`
> and you can iterate without a server round-trip. Revert this before
> merging — it's marked with a `TODO: remove` comment.

### Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| No log line at all, no plugin rendered | dataStore entry under wrong namespace/key, or user can't read it. Check sharing. |
| `HTTP 404/Connection refused` | URL unreachable from device. Verify `10.0.2.2` (emulator) vs LAN IP (device), and that `python3 -m http.server` is still running. |
| `Plugin checksum mismatch!` | Re-run `shasum -a 256`, paste with the `sha256:` prefix. The file you SHA'd must be the exact bytes the server serves. |
| `ClassCastException: … not assignable to Dhis2Plugin` | Plugin DEX bundles its own copy of the SDK. Every `plugin-sdk`/Compose/Material3 dep in `plugin/build.gradle.kts` must be `compileOnly`. |
| `ClassNotFoundException` on the plugin's entry point, downloaded bytes are huge (~17 MB) | You built the whole harness APK (`:app:assembleDebug`) instead of running `:plugin:buildPluginDex`, so the "DEX" is a multi-dex APK and the entry-point class isn't in `classes.dex`. Always use `:plugin:buildPluginDex`. |
| Plugin code change isn't visible | The cached DEX is still the old one. Bump `pluginVersion` in `plugin/build.gradle.kts` (and the dataStore JSON / hardcoded fallback) or `adb shell run-as … rm -rf files/plugins`. |
| `Plugin system requires API 26+` | The emulator/device is on API < 26. Use API 26+ image. |

---

*Source files for reference:*
`plugin-sdk/src/commonMain/kotlin/org/dhis2/mobile/plugin/sdk/` —
`Dhis2Plugin.kt`, `Dhis2PluginContext.kt`, `PluginMetadata.kt`,
`InjectionPoint.kt`; `plugin/src/main/java/org/dhis2/mobile/plugin/` —
`data/AppHubPluginRepository.kt`, `data/PluginDownloader.kt`,
`data/PluginVerifier.kt`, `data/PluginLoader.kt`,
`domain/LoadPluginsUseCase.kt`,
`security/ScopedDhis2PluginContext.kt`, `ui/PluginSlot.kt`.
