# DHIS2 Android Plugin System

> Status: preview (`plugin-sdk 0.1.0-SNAPSHOT`). API, bundle format, and
> injection points may still change.

## 1. What it is

Third-party developers can extend the DHIS2 Android Capture App without
forking it. A plugin is a small Android library that implements `Dhis2Plugin`,
is packaged as a **signed zip bundle** (DEX + resources), and is picked up at
login time via a server-side configuration.

Two audiences:

- **DHIS2 server administrators** — decide which plugins an instance uses, by
  writing a small JSON config into the server dataStore.
- **Third-party Android developers** — build Composable UI and domain logic
  that runs inside the Capture App under a scope-enforced SDK.

The host app does download, integrity + signature verification, sandboxing, DI
wiring, resource injection, and rendering. The plugin only provides metadata
and a Composable.

## 2. How it works

```
Developer      :plugin:buildPluginBundle  →  signed zip {id}-{version}.zip
Developer      uploads zip to a URL reachable by the device
DHIS2 admin    writes JSON to dataStore dhis2AndroidPlugins/config
Capture App    at login:  download → SHA-256 → JAR signature
                        → extract zip → load DEX via InMemoryDexClassLoader
                        → register instance
Capture App    at render: PluginSlot(slot) per plugin
                        → FileSystemResourceReader via LocalResourceReader
                        → ScopedDhis2PluginContext (allow-list enforcement)
                        → plugin.content(ctx)
```

## 3. The SDK (`:plugin-sdk`)

Maven coordinates: `org.dhis2.mobile:plugin-sdk:0.1.0-SNAPSHOT` (Maven Local
only for now). Kotlin Multiplatform — Android + Desktop JVM targets. Only the
Android host exists today; Desktop is a future host, no SDK changes needed.

### Public API

```kotlin
interface Dhis2Plugin {
    val metadata: PluginMetadata
    fun provideKoinModule(): Module? = null
    @Composable fun content(context: Dhis2PluginContext)
}

@Serializable
data class PluginMetadata(
    val id: String,                                 // "org.myorg.my-plugin"
    val version: String,                            // "1.0.0"
    val entryPoint: String,                         // "org.myorg.plugin.MyPlugin"
    val allowedProgramUids: List<String> = emptyList(),
    val allowedDataSetUids: List<String> = emptyList(),
    val injectionPoints: List<InjectionPoint> = emptyList(),
    val downloadUrl: String = "",
    val checksum: String = "",                      // "sha256:<hex>"
)

interface Dhis2PluginContext {
    val pluginMetadata: PluginMetadata
    suspend fun getTrackedEntityInstances(programUid: String):
        Result<List<TrackedEntityInstanceDto>>
    suspend fun getDataValues(orgUnitUid: String, dataSetUid: String, period: String):
        Result<List<DataValueDto>>
    suspend fun saveDataValue(dataSetUid: String, dataValue: DataValueDto):
        Result<Unit>
}

enum class InjectionPoint { HOME_ABOVE_PROGRAM_LIST }
```

Contract:

- The entry-point class must be **public** with a **no-arg constructor** — the
  host instantiates it via reflection.
- `content()` runs inside the host composition; don't navigate outside the slot.
- Every `Dhis2PluginContext` operation is scope-checked against the plugin's
  allow-lists. Out-of-scope access returns `Result.failure(SecurityException)`
  — never silently empty, never thrown.
- Plugins only see DTOs (`TrackedEntityInstanceDto`, `DataValueDto`), never
  raw SDK (`D2`) types.

## 4. Server-side configuration

The admin writes a JSON object into the DHIS2 server dataStore at:

- **namespace:** `dhis2AndroidPlugins`
- **key:** `config`

```json
{
  "plugins": [
    {
      "id": "org.myorg.my-plugin",
      "version": "1.0.0",
      "entryPoint": "org.myorg.plugin.MyPlugin",
      "downloadUrl": "https://example.com/my-plugin-1.0.0.zip",
      "checksum": "sha256:abc…",
      "allowedProgramUids": ["UID1"],
      "allowedDataSetUids": [],
      "injectionPoints": ["HOME_ABOVE_PROGRAM_LIST"]
    }
  ]
}
```

Edit via DHIS2 web's **Data Store Manager** app, or via the REST API:

```bash
curl -u <user:pass> -X POST \
  -H "Content-Type: application/json" \
  "https://<server>/api/dataStore/dhis2AndroidPlugins/config" \
  --data @config.json
```

(POST first time, PUT to update.) Plugins the app can't download or verify
are skipped silently; the rest still load.

## 5. Writing a plugin

The sample project at `AndroidStudioProjects/Pluginimplementationtest` shows
the reference setup with two modules:

- **`:plugin`** — KMP + CMP library. Contains the plugin code and resources.
  Produces the shippable signed zip.
- **`:app`** — plain Android application harness for previewing without
  installing the Capture App.

### 5.1 `:plugin/build.gradle.kts` essentials

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)   // NOT com.android.library
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidLibrary {
        namespace = "org.myorg.myplugin.plugin"
        compileSdk = 36
        minSdk = 26                                // InMemoryDexClassLoader floor
    }
    sourceSets.commonMain.dependencies {
        compileOnly(libs.plugin.sdk)               // host provides at runtime
        compileOnly(compose.runtime)
        compileOnly(compose.ui)
        compileOnly(compose.material3)
        // MUST be `implementation` — this is CMP's opt-in signal to generate
        // the Res accessor. With compileOnly, Res.* imports don't resolve.
        implementation(compose.components.resources)
    }
}

compose.resources {
    // Set explicitly or CMP derives it from the root project name.
    packageOfResClass = "org.myorg.myplugin.plugin.generated.resources"
}

// buildPluginBundle task — ~130 lines, copy from the sample.
```

### 5.2 Implement `Dhis2Plugin`

```kotlin
package org.myorg.myplugin

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.dhis2.mobile.plugin.sdk.*
import org.myorg.myplugin.plugin.generated.resources.Res
import org.myorg.myplugin.plugin.generated.resources.plugin_title
import org.jetbrains.compose.resources.stringResource

class MyPlugin : Dhis2Plugin {
    override val metadata = PluginMetadata(
        id = "org.myorg.myplugin",
        version = "1.0.0",
        entryPoint = "org.myorg.myplugin.MyPlugin",
        allowedProgramUids = listOf("IpHINAT79UW"),
        injectionPoints = listOf(InjectionPoint.HOME_ABOVE_PROGRAM_LIST),
    )

    @Composable
    override fun content(context: Dhis2PluginContext) {
        Text(stringResource(Res.string.plugin_title))
    }
}
```

### 5.3 Resources

```
plugin/src/commonMain/composeResources/
├── values/strings.xml           # default (English)
├── values-es/strings.xml        # Spanish
└── drawable/plugin_icon.xml
```

Access from code: `stringResource(Res.string.foo)`, `painterResource(Res.drawable.foo)`.

### 5.4 Build the bundle

```bash
./gradlew :plugin:buildPluginBundle
```

Output: `plugin/build/outputs/plugin-bundle/{id}-{version}.zip`. The task prints
size + SHA-256. Signing uses the Android debug keystore (`~/.android/debug.keystore`,
password `android`). Production publishers swap to their own keystore.

Bundle layout:

```
{id}-{version}.zip
├── META-INF/…              (jarsigner)
├── plugin.json             (id, version, entryPoint, targets)
└── android/
    ├── classes.dex         (plugin classes only — host provides the rest)
    └── composeResources/{packageOfResClass}/…
```

The `android/` prefix is deliberate — adding a future Desktop target means
adding `desktop/plugin.jar` alongside, not a new distribution format.

### 5.5 Ship it

Upload the zip to a URL the device can reach, and add an entry to the
dataStore JSON (§4). Done.

## 6. Security model

- **Scope enforcement.** `Dhis2PluginContext` rejects programs/datasets not in
  the plugin's allow-list (`Result.failure(SecurityException)`).
- **DTO boundary.** Plugins never see `D2`. Insulates plugins from SDK
  evolution and prevents escape via the SDK's fluent API.
- **Integrity.** SHA-256 verified before load. Mismatch evicts the cache.
- **Authorship.** JAR signature verified via standard `jarsigner` scheme. Any
  valid signature passes today — per-publisher cert allow-listing is future work.
- **API guard.** `InMemoryDexClassLoader` requires API 26+; older devices skip
  the whole plugin system (log + empty registry).
- **Process.** Plugins run **in-process** with the host. A crash propagates to
  the enclosing composition — pick trusted authors.

## 7. Current limitations

- One injection point: `HOME_ABOVE_PROGRAM_LIST`.
- `buildPluginBundle` is copy-pasted per plugin project — no published Gradle
  plugin yet.
- No plugin uninstall flow — delete the dataStore entry and the device cache
  (`/data/data/com.dhis2.debug/files/plugins/{id}-{version}.zip`).
- No per-publisher cert allow-list.
- Plugins share the host's `D2` session and Koin graph — a misbehaving Koin
  binding in a plugin can affect the host.
- `Dhis2PluginContext` exposes only TEIs and data values; events, enrollments,
  and org-units are future work.
- No `plugin-sdk-test` artefact — plugin authors copy-paste their own
  `StubDhis2PluginContext` for previews.

## 8. Testing a plugin locally

Android emulator + local Python HTTP server + the sample project.

1. **Publish the SDK to Maven Local** (host repo):

   ```bash
   cd ~/StudioProjects/ai-dhis2-android-capture-app
   ./gradlew :plugin-sdk:publishToMavenLocal
   ```

2. **Build the bundle** (sample repo):

   ```bash
   cd ~/AndroidStudioProjects/Pluginimplementationtest
   ./gradlew :plugin:buildPluginBundle
   ```

   Printed output gives the zip path and SHA-256.

3. **Serve it to the emulator**:

   ```bash
   cd plugin/build/outputs/plugin-bundle
   python3 -m http.server 8080
   ```

   From the emulator: `http://10.0.2.2:8080/{id}-{version}.zip`. For a physical
   device on the same LAN, use the host's LAN IP instead of `10.0.2.2`.

4. **Point the Capture App at the bundle**, two options:

   - **Real path**: write the JSON (§4) to the DHIS2 server dataStore. Paste
     the SHA-256 as `checksum`. For a first smoke test `"checksum": ""` works
     (SHA-256 is skipped with a warning; signature verification still runs).
   - **Fast local iteration**: edit `FALLBACK_CONFIG_JSON` in
     `plugin/src/main/java/org/dhis2/mobile/plugin/data/AppHubPluginRepository.kt`.
     It's used when the dataStore has no config. Marked `TODO: remove` —
     revert before merging.

5. **Run the Capture App** (`dhis2Debug` variant) and log in. Watch the logs:

   ```bash
   adb logcat | grep -E "Plugin|Dhis2Plugin"
   ```

   Expected sequence:

   ```
   Downloading plugin 'org.dhis2.myplugin' v1.2.0 from http://…
   Loading plugin 'org.dhis2.myplugin' v1.2.0 from DEX (N bytes) with resource root …
   Plugin 'org.dhis2.myplugin' v1.2.0 loaded successfully
   ```

   The plugin renders above the program list.

6. **Iterate.** After code changes: bump `pluginVersion` in
   `plugin/build.gradle.kts` and in the JSON/fallback, rebuild, restart the
   app. Or wipe the device cache:

   ```bash
   adb shell run-as com.dhis2.debug rm -rf files/plugins
   ```

7. **Locale test.** Switch the emulator language (Settings → Languages) and
   reopen the screen. The plugin's strings should change accordingly.

### Previewing with the harness (optional)

`./gradlew :app:installDebug` builds a standalone preview app that
instantiates `MyPlugin` with a `StubDhis2PluginContext` (fake TEIs). Use it
for quick UI tweaks without a Capture App rebuild.

## 9. Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `No plugin configuration found in server dataStore` | Config isn't at `dhis2AndroidPlugins/config` or the user can't read the namespace. Use the `FALLBACK_CONFIG_JSON` hack during iteration. |
| `Plugin checksum mismatch!` | The served zip doesn't match `checksum` in the config. Re-run `shasum -a 256` and update the JSON (with `sha256:` prefix). |
| `Plugin bundle signature verification failed` / `Unsigned entry in plugin bundle` | The zip was edited after signing. Re-run `:plugin:buildPluginBundle`; never hand-edit the zip. |
| `ClassCastException: … not assignable to Dhis2Plugin` | Plugin DEX bundles its own SDK copy. Keep `plugin-sdk` + all `compose.*` deps (except `compose.components.resources`) as `compileOnly`. |
| `NoSuchMethodError` for mangled `Text`/`Card` signatures | Compose ABI mismatch. Plugin is compiled against CMP 1.10.3; consumer is on a different version. Harness `:app` and the Capture App must both use CMP (`compose.runtime` etc.), not `androidx.compose.bom`. |
| `MissingResourceException` for `composeResources/…` | Capture App: `PluginSlot` should provide `LocalResourceReader` per-plugin. Harness: the `stagePluginAssets` task must run and stage resources into `:app`'s assets. |
| Plugin code changes aren't visible | Cached bundle. Bump `pluginVersion` or `adb shell run-as com.dhis2.debug rm -rf files/plugins`. |
| `Plugin system requires API 26+` | Device/emulator is API < 26. Use an API 26+ image. |

---

*Source files for reference:*

- `plugin-sdk/src/commonMain/kotlin/org/dhis2/mobile/plugin/sdk/` — `Dhis2Plugin.kt`, `Dhis2PluginContext.kt`, `PluginMetadata.kt`, `InjectionPoint.kt`, `dto/*`
- `plugin/src/main/java/org/dhis2/mobile/plugin/` — `data/AppHubPluginRepository.kt`, `data/PluginDownloader.kt`, `data/PluginVerifier.kt`, `data/PluginLoader.kt`, `domain/LoadPluginsUseCase.kt`, `registry/PluginRegistry.kt`, `security/ScopedDhis2PluginContext.kt`, `ui/PluginSlot.kt`, `ui/FileSystemResourceReader.kt`
- Sample project: `~/AndroidStudioProjects/Pluginimplementationtest/`
