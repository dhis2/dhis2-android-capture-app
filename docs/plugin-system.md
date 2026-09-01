# DHIS2 Android Plugin System

> Status: preview (`plugin-sdk 0.1.2-SNAPSHOT`). API, bundle format, and
> injection points may still change.

## 1. What it is

Third-party developers can extend the DHIS2 Android Capture App without
forking it. A plugin is a small Android library that implements `Dhis2Plugin`,
is packaged as a **signed zip bundle** (DEX + resources), and is picked up when the
home screen opens, from a server-side configuration.

Two audiences:

- **DHIS2 server administrators** — decide which plugins an instance uses, by
  writing a small JSON config into the server dataStore.
- **Third-party Android developers** — build Composable UI and domain logic
  that runs inside the Capture App with the DHIS2 SDK available to it.

The host app does download, integrity and signature verification, DI wiring, resource
injection, and rendering. The plugin only provides a Composable — its identity lives in
the server config, not in the plugin.

A plugin runs with the host's full SDK authority and in the host's own process; **§6 is the
part to read before enabling this on a real instance.**

## 2. How it works

```
Developer      :plugin:buildPluginBundle  →  signed zip {module}-{version}.zip
Developer      uploads zip to a URL reachable by the device
DHIS2 admin    writes JSON to dataStore dhis2AndroidPlugins/config
Capture App    on opening Home:  refresh dhis2AndroidPlugins namespace
                        → read config → download → SHA-256 → JAR signature
                        → extract zip → load DEX via InMemoryDexClassLoader
                        → build the context (D2 + metadata)
                        → build a private Koin container for the plugin
                        → register instance
Capture App    at render: PluginSlot(slot) per plugin, keyed on its class loader
                        → FileSystemResourceReader via LocalResourceReader
                        → KoinIsolatedContext(plugin's own container)
                        → plugin.content(ctx)
```

Currently, loading is triggered from `MainViewModel`, so it runs when the home screen opens rather
than during sync. In the future, plugins will be downloaded during the synchronization.

## 3. The SDK (`:plugin-sdk`)

### The Gradle plugin (`:plugin-sdk-gradle`)

Plugin id `org.dhis2.mobile.plugin-bundle`, published from `:plugin-sdk-gradle` at the same version
as the SDK (`libs.versions.pluginSdk` drives both, so they can never drift).

Applying it registers `buildPluginBundle` (§5.3), adds `plugin-sdk` as a `compileOnly` dependency
at the matching version, and checks the plugin's toolchain against the host (§5.1). §5 has the
lines a plugin project declares.

It has to be a second artifact rather than something inside `plugin-sdk`: that artifact sits on a
plugin's *compile* classpath, while Gradle resolves build logic from a separate classpath — and
Kotlin Multiplatform projects cannot apply the `java-gradle-plugin` at all
(`KMPJavaPluginsIncompatibilityDiagnostic`, severity error). Two published artifacts, one
declaration.

### Public API

```kotlin
interface Dhis2Plugin {
    fun provideKoinModule(): Module? = null
    @Composable
    fun content(context: Dhis2PluginContext)
}
```

It is the main entry point for a DHIS2 mobile plugin.
In practice a whole plugin is one Composable:

```kotlin
class MyPlugin : Dhis2Plugin {
    @Composable
    override fun content(context: Dhis2PluginContext) {
        Card { Text("Hello from a plugin") }
    }
}
```

Optionally provide a Koin module with the plugin's own dependencies (ViewModels, repositories, use
cases, etc.). Those bindings go into a container private to the plugin — never the host's (§6).

### PluginMetadata

Describes a plugin's identity, version, and distribution metadata.

**Server-owned.** The DHIS2 administrator authors this as JSON in the server dataStore
(namespace `dhis2AndroidPlugins`, key `config`); it is the single source of truth. Plugins do
not declare any of it — the host reads it to decide what to download, verify and load, then
hands it to the plugin through `[Dhis2PluginContext.pluginMetadata]`, so there is exactly one place
to change a plugin's identity and a plugin cannot rename itself into someone else's configuration.

There is deliberately **no data-scope field.** An earlier version carried `allowedProgramUids` /
`allowedDataSetUids`; they were removed when the API began exposing the SDK, because a grant nothing
enforces is worse than no grant — it reads like a control. Configs still carrying them keep loading:
unknown keys are ignored.

```kotlin
@Serializable
data class PluginMetadata(
    val id: String,                                 // "org.myorg.my-plugin"
    val version: String,                            // "1.0.0"
    val entryPoint: String,                         // "org.myorg.plugin.MyPlugin"
    val injectionPoints: List<InjectionPoint> = emptyList(),
    val downloadUrl: String = "",
    val checksum: String = "",                      // "sha256:<hex>"
)
```

### Injection points
Named slots in the host app where a plugin's Composable UI can be rendered.

A plugin declares the slots it targets in [PluginMetadata.injectionPoints], and the host renders
them at each slot via `PluginSlot`. There is one slot today (§7); more can be added as they are
needed.

```kotlin
enum class InjectionPoint {
    /** Rendered on the home screen, immediately above the program list. */
    HOME_ABOVE_PROGRAM_LIST,
}
```

### Plugin context

The gateway through which a plugin reaches DHIS2 data. A plugin receives one as the parameter to
`Dhis2Plugin.content`.

```kotlin
interface Dhis2PluginContext {
    val pluginMetadata: PluginMetadata
    val sdk: D2
}
```

`sdk` is **the DHIS2 Android SDK itself**, unrestricted. A plugin gets the whole fluent API — filters,
ordering, paging, children, `blockingGet()` — with no wrapper in the way:

```kotlin
val overdue = context.sdk.eventModule().events()
    .byStatus().eq(EventStatus.OVERDUE)
    .byOrganisationUnitUid().eq(clinicUid)
    .orderByDueDate(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()
```

**There is no data-access control**: a plugin can read and write anything the logged-in user can.
That is deliberate for this iteration — see §6 for what follows from it.

Two consequences of exposing the SDK rather than DTOs:

- **A plugin is pinned to the host's SDK version.** The plugin ABI now includes the DHIS2 SDK's, so
  the plugin-bundle Gradle plugin injects `org.hisp.dhis:android-core` at the host's version
  (§5.1) — a plugin never declares it, and so cannot drift onto a version whose methods are not
  there at runtime.
- **`Dhis2Plugin` and `Dhis2PluginContext` live in `plugin-sdk`'s `androidMain`**, because `D2` is the
  Android SDK and has no common-source equivalent. Write your plugin class in `src/androidMain`;
  `PluginMetadata` and `InjectionPoint` stay in `commonMain`.

`blockingGet()` and friends must not run on the main thread — wrap them in `Dispatchers.IO`.

Contract:

- A plugin declares **no identity of its own** — no id, version or entry point. All of it comes from
  the server config, so there is exactly one place to change it. The one apparent exception is
  cosmetic: `pluginBundle { pluginId; entryPoint }` (§5.3) fills in the generated
  `plugin-config.json` and reaches nothing else — not the bundle, not the host, which reads both
  from the dataStore.
- The entry-point class must be **public** with a **no-arg constructor** — the host instantiates it
  via reflection.
- `content()` runs inside the host composition; don't navigate outside the slot.
- **Budget your height.** `HOME_ABOVE_PROGRAM_LIST` is a plain, non-scrolling `Column` sitting above
  the host's own scrolling program list, so every pixel a plugin takes is a pixel the host loses, and
  anything past the viewport is unreachable rather than scrollable. Keep the resting state short, put
  detail behind a toggle, and if a section can grow, bound it (`heightIn(max = …)` plus
  `verticalScroll`) so it scrolls inside the plugin instead of pushing the host's content off screen.
- **Composition state does not survive a plugin reload.** The load pipeline runs more than once per
  process — a metadata sync is enough — and each run builds a fresh class loader, so the plugin's
  classes are replaced wholesale. `PluginSlot` keys the composition on that loader and discards the
  previous one, because state remembered across the swap would be an instance of the *old* loader's
  class and any cast to the new one fails. Keep anything that must outlive a reload out of the
  composition.
- **`koinInject` and `koinViewModel` resolve against the plugin's own private container**, seeded
  with its `D2`, `PluginMetadata` and context. Nothing host-owned leaks in, and — more importantly —
  nothing the plugin binds leaks *out* into the host's container.

## 4. Server-side configuration

### 4.1 The config entry

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
      "injectionPoints": [
        "HOME_ABOVE_PROGRAM_LIST"
      ]
    }
  ]
}
```

Removing a plugin is deleting its entry from this array (and see §7 for the device-side cache).

### 4.2 Who may write it

**This key names code that every device on the instance will execute**, so write access to it is
the whole trust boundary of the plugin system (§6). DHIS2 creates dataStore keys publicly
writable by default, so it does not start restricted.

Restrict the `dhis2AndroidPlugins` namespace to administrators using your server's dataStore
sharing settings, and confirm two things afterwards: that an ordinary user *cannot* write the key,
and that they *can* still read it — the app reads this namespace as the logged-in user, so a key
they cannot read means no plugins load for them (§9).

The exact sharing mechanism differs by DHIS2 version; check the dataStore sharing documentation
for the version you run rather than assuming the default is safe.

## 5. Writing a plugin

A plugin is **its own Gradle project**, not a module of the Capture App: one Kotlin Multiplatform
library module, no application module, nothing added to this repo. Everything below is complete —
create these files in an empty directory with a Gradle 9.5+ wrapper and you have a working plugin.

```
my-plugin/
├── settings.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/…                                   Gradle 9.5+ (AGP 9.3.1 requires it)
└── plugin/
    ├── build.gradle.kts
    └── src/
        ├── androidMain/kotlin/org/myorg/myplugin/MyPlugin.kt
        └── commonMain/composeResources/values/strings.xml   optional
```

The plugin class goes in **`androidMain`**, not `commonMain`: `Dhis2PluginContext.sdk` is `D2`, the
DHIS2 *Android* SDK, which has no common-source equivalent. Compose resources stay in `commonMain`,
where the resource generator expects them — the generated `Res` class is visible from `androidMain`.

### 5.1 Set up the build

**`settings.gradle.kts`** — while the plugin system is in preview both artifacts come from Maven
Local, so `mavenLocal()` is needed in *both* repository blocks: `pluginManagement` resolves the
plugin id, `dependencyResolutionManagement` resolves the `plugin-sdk` it pulls in.

```kotlin
pluginManagement {
    repositories {
        mavenLocal()          // ← org.dhis2.mobile.plugin-bundle
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()          // ← org.dhis2.mobile:plugin-sdk
        google()
        mavenCentral()
        // A plugin now compiles against org.hisp.dhis:android-core, which pulls
        // com.github.dhis2:sms-compression from JitPack. Without this the build fails at dependency
        // *resolution*, with an error that never mentions the DHIS2 SDK.
        maven("https://jitpack.io")
        // The host tracks SDK snapshots, so the injected android-core version is usually a snapshot.
        maven("https://central.sonatype.com/repository/maven-snapshots")
    }
}

rootProject.name = "my-plugin"
include(":plugin")
```

**`gradle/libs.versions.toml`** — the versions in the table below, and one version shared by both
DHIS2 artifacts:

```toml
[versions]
agp = "9.3.1"
kotlin = "2.4.10"                 # must equal the host's
composeMultiplatform = "1.10.3"   # must equal the host's
pluginSdk = "0.1.2-SNAPSHOT"

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
android-kotlin-multiplatform-library = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
dhis2-pluginBundle = { id = "org.dhis2.mobile.plugin-bundle", version.ref = "pluginSdk" }
```

No `[libraries]` entry for `plugin-sdk` is needed — the Gradle plugin adds that dependency itself.
Declare one only if something else in your build compiles against the SDK directly, such as the
optional preview app in §8.

**`plugin/build.gradle.kts`** — the whole file. `version` is the only plugin-specific setting;
everything else is ordinary KMP/CMP wiring.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)   // NOT com.android.library
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dhis2.pluginBundle)                     // registers buildPluginBundle
}

version = "1.0.0"                                  // names the bundle; see §5.3 for the rest

kotlin {
    androidLibrary {
        namespace = "org.myorg.myplugin"
        compileSdk = 37                            // >= the host's
        minSdk = 26                                // InMemoryDexClassLoader floor
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    // Future Desktop host: add `jvm("desktop")` here.

    sourceSets {
        val commonMain by getting {
            dependencies {
                // The host provides these through the parent class loader at runtime. A second copy
                // inside the plugin DEX is what produces ClassCastException / NoSuchMethodError.
                compileOnly(compose.runtime)
                compileOnly(compose.ui)
                compileOnly(compose.material3)
                // Must be `implementation`: the Compose Resources plugin uses this declaration as
                // its opt-in signal to generate the `Res` accessor class, and with `compileOnly`
                // every `Res.string.*` import fails to resolve. The runtime classes still come
                // from the host.
                implementation(compose.components.resources)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "org.myorg.myplugin.generated.resources"
    publicResClass = true
}
```

Without a version catalog, the plugin line is
`id("org.dhis2.mobile.plugin-bundle") version "0.1.2-SNAPSHOT"`.

`plugin-sdk` is **not** declared as a dependency: the plugin-bundle plugin adds it as `compileOnly`
at its own version, so it can never be pinned to the wrong one.

**Toolchain versions must track the host.** The plugin's DEX loads into the Capture App's
process and resolves Kotlin, Compose and the SDK from the *host's* class loader, and
`plugin-sdk` is published with the host's Kotlin metadata version and `compileSdk`. Note the
constraint differs per row — some must match the host exactly, others are bounds:

| Setting        | Host today                        | Your plugin must be              | If you get it wrong                                                                                             |
|----------------|-----------------------------------|----------------------------------|-----------------------------------------------------------------------------------------------------------------|
| `kotlin`       | `2.4.10`                          | **equal**                        | `Module was compiled with an incompatible version of Kotlin` — an older compiler cannot read the SDK's metadata |
| Compose plugin | `1.10.3` (`composePluginVersion`) | **equal**                        | `NoSuchMethodError` on mangled `Text`/`Card` signatures                                                         |
| `compileSdk`   | `37`                              | **>=** host                      | `checkAarMetadata` fails on `plugin-sdk-android`                                                                |
| `minSdk`       | `23`                              | **>= 26** (not the host's)       | `InMemoryDexClassLoader` needs API 26; the host itself supports 23, but a plugin cannot load below 26           |
| JVM target     | `17`                              | **<=** host                      | `Unsupported class file major version` when the DEX is loaded. Lower is safe, higher is not                     |
| AGP            | `9.3.1`                           | new enough for that `compileSdk` | build fails on an unknown `compileSdk`                                                                          |
| Gradle wrapper | `9.5.1`                           | **>= 9.5.0** for AGP 9.3.1       | AGP refuses to run                                                                                              |

The first five rows are **checked at configuration time** by the plugin-bundle plugin, which fails
the build listing every mismatch and what it would have caused. `--info` logs what it detected; a
value it cannot read is skipped rather than guessed at. `pluginBundle { verifyToolchain = false }`
opts out and trades these build errors for runtime failures on device.

Values above are generated into the Gradle plugin from `gradle/libs.versions.toml`, so the checks
move with each host release; treat that file as the source of truth.

A Kotlin mismatch is usually followed by a cascade of `Unresolved reference 'lazy'` in the
generated resource accessors — that cascade is a symptom, not the cause; fix the version.

One thing the checks can only warn about: an AAR's `classes.jar` holds *only* your module's
classes, so any `implementation`/`api` dependency is compiled against but never packaged, and has
to already exist in the host process. Keep everything the host provides — `plugin-sdk` and all
`compose.*` except `compose.components.resources` — on `compileOnly`.

### 5.2 Implement `Dhis2Plugin`

In `src/androidMain/kotlin/org/myorg/myplugin/MyPlugin.kt`:

```kotlin
package org.myorg.myplugin

class MyPlugin : Dhis2Plugin {
    @Composable
    override fun content(context: Dhis2PluginContext) {
        val events by produceState(emptyList<Event>(), context) {
            value = withContext(Dispatchers.IO) {
                context.sdk.eventModule().events()
                    .byStatus().eq(EventStatus.OVERDUE)
                    .blockingGet()
            }
        }
        Text(stringResource(Res.string.plugin_title, events.size))
    }
}
```

That is the whole plugin. The id, version, entry-point class name and injection points are the
server admin's to declare (§4) — the plugin restates none of them, and reads them back from
`context.pluginMetadata` if it needs them.

`context.sdk` is the SDK's full API, and `blockingGet()` must not run on the main thread — hence the
`Dispatchers.IO` wrapper. Keep data access out of your Composables where you can: a Composable that
takes plain data is one you can render in a `@Preview` without a context.

### 5.3 Build the bundle

```bash
./gradlew :plugin:buildPluginBundle
```

The task comes from the plugin-bundle Gradle plugin (§3) — there is nothing to copy into your
build. It writes three files to `plugin/build/outputs/plugin-bundle/`:

```
{module}-{version}.zip           the bundle, named from the Gradle module and its `version`
{module}-{version}.zip.sha256    its checksum, in the `sha256:<hex>` form the config wants
plugin-config.json              the §4 dataStore entry, checksum and version already filled in
```

The zip's file name is only a convenience for whoever hosts it; the host locates the bundle by the
config's `downloadUrl` and identifies it by the config's `id`/`version`, so it can be called
anything.

`plugin-config.json` is rewritten on every build, so editing it in place does not survive. Its
`id` and `entryPoint` are the only fields the build cannot work out for itself, and they default to
obvious placeholders (`org.myorg.my-plugin`, `org.myorg.myplugin.MyPlugin`) — declare them once and
the file is postable as it is:

```kotlin
pluginBundle {
    pluginId = "org.myorg.my-plugin"
    entryPoint = "org.myorg.plugin.MyPlugin"
}
```

Neither value reaches the bundle. They are there so the checksum and the identity arrive together
in one postable file; the dataStore is still the only thing the host reads identity from.

Bundle layout:

```
{module}-{version}.zip
├── META-INF/…              (apksigner, v1/JAR signature scheme)
└── android/
    ├── classes.dex         (plugin classes only — host provides the rest)
    └── composeResources/{packageOfResClass}/…
```

The `android/` prefix is deliberate — adding a future Desktop target means
adding `desktop/plugin.jar` alongside, not a new distribution format.

**The bundle is reproducible**: identical sources produce identical bytes, so the SHA-256 only
changes when the plugin does. Two things are needed for that and both are handled by the task —
entries are written sorted with a fixed timestamp, and signing uses `apksigner` rather than
`jarsigner`, whose PKCS#7 block embeds a signed `signingTime` that changes the bytes on every run.

Signing defaults to the Android debug keystore (`~/.android/debug.keystore`, password `android`).
Production publishers point it at their own key; passwords are passed to `apksigner` through the
environment, not argv:

```kotlin
pluginBundle {
    signing {
        keystore = file("release.keystore")
        alias = "publisher"
        storePassword = providers.gradleProperty("pluginKeystorePassword")
        keyPassword = providers.gradleProperty("pluginKeyPassword")
    }
}
```

Everything else in `pluginBundle { }` is optional: `pluginId` / `entryPoint` (above),
`resourcePackage` (read back from the compiled `Res` class by default), `minApi` (26),
`bundleFileName`, `outputDirectory`, `d8Executable` / `apksignerExecutable` (found in the SDK's
newest build-tools), `verifyToolchain` (§5.1) and `emitDataStoreSnippet`.

### 5.4 Ship it

Upload the zip to a URL the device can reach, and add an entry to the
dataStore JSON (§4). Done.

## 6. Security model

**Read this first: a plugin gets the DHIS2 SDK unrestricted.** `Dhis2PluginContext.sdk` is `D2`
itself, so a loaded plugin can read and write anything the logged-in user can — including
`d2.wipeModule()`. There is no data-access control in this iteration, and the previous
`allowedProgramUids` / `allowedDataSetUids` allow-list has been removed rather than left in place,
because it only ever covered three DTO methods and would now read as a control that does not exist.

**So the security decision is which plugins you run.** Everything below protects the *integrity of
the delivery pipeline* — that the code executed is the code the administrator intended, unmodified —
not what that code may then touch. Narrowing access is the next iteration of this work, and belongs
in the SDK rather than here, so a future out-of-process host can sit in front of the same
enforcement.

- **Integrity.** SHA-256 verified before load. Mismatch evicts the cache.
- **Config write access.** The dataStore key names the code the app will execute, and DHIS2 creates
  dataStore keys publicly writable. Whoever can write that key can add a plugin to every device on
  the instance, so restrict it to administrators (§4.2).
- **Authorship.** JAR signature (v1 scheme) verified with `JarFile(bundle, verify = true)`; every
  entry must be covered. Any valid signature passes today — per-publisher cert allow-listing is
  future work.
- **API guard.** `InMemoryDexClassLoader` requires API 26+; older devices skip
  the whole plugin system (log + empty registry).
- **Host DI integrity.** Each plugin gets a **private Koin container**, seeded only with its own
  `D2`, `PluginMetadata` and context. Plugin modules used to be loaded into the *application*
  container, and because Koin allows override by default a plugin declaring a binding for a type the
  host also binds would silently *replace* it for the rest of the app — one plugin breaking unrelated
  screens, with nothing to say so.
- **Process.** Plugins run **in-process** with the host. A crash propagates to the enclosing
  composition, and Compose cannot express an error boundary around a composable call — the compiler
  rejects `try`/`catch` there, because recomposition has no way to unwind a partially-applied
  composition. Pick trusted authors.

## 7. Current limitations

- **Unrestricted SDK access, no crash isolation, and no certificate pinning** — the three that
  decide whether this is safe to enable, covered in §6. Narrowing access is the next iteration and
  belongs in the SDK, not the host.
- **No uninstall or kill switch** — delete the dataStore entry (§4.1) and the device cache
  (`/data/data/com.dhis2.debug/files/plugins/{id}-{version}.zip` — the cache is named from
  the config's id/version, not the served filename). There is no way to disable a
  misbehaving plugin remotely — which matters more than it looks, given the bullet above.
- One injection point: `HOME_ABOVE_PROGRAM_LIST`. More can be added as consumers need them.
- The plugin-bundle Gradle plugin is published to Maven Local only — not yet to Maven Central or
  the Gradle Plugin Portal, so plugin authors need `mavenLocal()` in `pluginManagement`.
- **A plugin cannot stub its own context for previews**, and there is no `plugin-sdk-test`
  artefact. `Dhis2PluginContext.sdk` is `D2`, which cannot be constructed outside a logged-in
  app — so a preview harness renders the plugin's own composables against sample UI state
  instead, which is why keeping the SDK-touching code behind a repository interface (§5.2)
  is what makes a plugin testable at all.
- **A plugin must compile against the host's exact androidx Compose versions.** Matching
  `composeMultiplatform` is not sufficient: the host resolves androidx Compose separately and
  higher, and a plugin built against a lower `foundation` crashes at composition with
  `NoSuchMethodError` on a synthetic default method (`Modifier.weight` is the one that bites).
  The bundle plugin pins the SDK version for you; Compose is still on the author.

## 8. Testing a plugin locally

What you need: this repo, your plugin project (§5), an API 26+ emulator or device, a DHIS2 server
you can write a dataStore namespace on, and a local static file server.

1. **Publish the SDK and its Gradle plugin to Maven Local** — in *this* repo:

   ```bash
   ./gradlew :plugin-sdk:publishToMavenLocal :plugin-sdk-gradle:publishToMavenLocal
   ```

   Both, always: the `id("org.dhis2.mobile.plugin-bundle")` line resolves from Maven Local, and it
   is what pulls in the matching `plugin-sdk`. Your plugin project needs `mavenLocal()` in both its
   `pluginManagement` and `dependencyResolutionManagement` repositories (§5.1).

2. **Build the bundle** — in your plugin project:

   ```bash
   ./gradlew :plugin:buildPluginBundle
   ```

   Printed output gives the zip path, its SHA-256 and the path of the generated
   `plugin-config.json`.

   **The SHA-256 only changes when the plugin does** — the bundle is reproducible (§5.3), so an
   unchanged plugin keeps its checksum and the dataStore config does not need re-editing on every
   rebuild. When the plugin *has* changed, copy the new checksum from `plugin-config.json` or the
   `.sha256` file. `"checksum": ""` skips the SHA-256 check with a warning while still enforcing the
   signature, which is a quicker loop while iterating on UI.

3. **Serve it to the emulator.** Pick a port that nothing else is using — a local
   DHIS2 instance usually owns `8080`, and serving the bundle from a port already
   taken by DHIS2 is the single most common cause of a plugin silently not loading
   (see the troubleshooting table below):

   ```bash
   # confirm the port is free first — no output means free
   lsof -nP -iTCP:8081 -sTCP:LISTEN

   cd plugin/build/outputs/plugin-bundle
   python3 -m http.server 8081
   ```

   Sanity-check that the port really serves the zip, and that its hash is the one
   you will put in the config:

   ```bash
   curl -s http://localhost:8081/{module}-{version}.zip | shasum -a 256
   ```

   From the emulator: `http://10.0.2.2:8081/{module}-{version}.zip`. For a physical
   device on the same LAN, use the host's LAN IP instead of `10.0.2.2`.

   The server must stay running for as long as you are testing — the bundle is
   re-downloaded whenever the on-device cache is wiped or the version changes.

4. **Point the Capture App at the bundle.**

   Write the JSON (§4) to the DHIS2 server dataStore. `plugin-config.json` next to the bundle is
   that JSON with `version` and `checksum` already filled in, plus `id` and `entryPoint` if you
   declared them (§5.3) — fill in what is left, which is `downloadUrl`, then post it:

   ```bash
   # first time — POST creates the key; use PUT to update it afterwards
   curl -u <user>:<pass> -H 'Content-Type: application/json' \
     -d @plugin-config.json \
     "https://<server>/api/dataStore/dhis2AndroidPlugins/config"
   ```

   The dataStore is the only source of plugin config; there is no in-app fallback. If the app logs
   `No plugin configuration found in server dataStore`, see §9.

5. **Run the Capture App** and log in — from this repo:

   ```bash
   ./gradlew :app:installDhis2Debug
   adb logcat | grep -E "Plugin|Dhis2Plugin"
   ```

   Expected sequence:

   ```
   Found 1 plugin(s) in server configuration
   Loading 1 plugin(s)
   Downloading plugin 'org.dhis2.myplugin' v1.2.0 from http://…
   Plugin cached to /data/user/0/com.dhis2.debug/files/plugins/…zip
   Loading plugin 'org.dhis2.myplugin' v1.2.0 from DEX (N bytes) with resource root …
   Plugin 'org.dhis2.myplugin' v1.2.0 loaded successfully
   ```

   The first two lines tell you the config was read at all — if they are missing, the
   problem is the dataStore config, not the bundle. On later launches the download
   lines are replaced by `Plugin '…' v… loaded from cache`.

   The plugin renders above the program list.

6. **Iterate.** After code changes: bump `version` in the plugin's
   `build.gradle.kts` and the matching `version` in the dataStore JSON, rebuild,
   restart the app. Or wipe the device cache:

   ```bash
   adb shell run-as com.dhis2.debug rm -rf files/plugins
   ```

7. **Locale test.** Switch the emulator language (Settings → Languages) and
   reopen the screen. The plugin's strings should change accordingly.

### Previewing without the Capture App (optional)

The loop above is slow for UI work. You can add an Android application module to *your own* plugin
project that renders the plugin's Composables directly with sample data.

Note it cannot render `Dhis2Plugin.content` itself: that needs a `Dhis2PluginContext`, whose `sdk`
is a `D2` that cannot be constructed outside a logged-in app, and there is no `plugin-sdk-test`
artefact (§7). This is the practical reason to keep SDK access behind a repository interface and
your Composables taking plain data (§5.2) — the part worth previewing is then the part with no
context in it. Two things that trip this up:

- Use Compose Multiplatform artifacts (`compose.runtime`, `compose.ui`, `compose.material3`), never
  `androidx.compose.bom` — the two ABIs are incompatible and the plugin's Composables fail with
  `NoSuchMethodError`.
- Compose resources have to be reachable. In the Capture App they are read from the extracted bundle
  by `PluginSlot`; a preview app instead needs the plugin's generated `composeResources/{package}/…`
  copied into its own assets, since that is where the default Android resource reader looks.

## 9. Troubleshooting

| Symptom                                                                                     | Cause / fix                                                                                                                                                                                                                                                                                                                                                                                              |                                                                                                                                                                                    |
|---------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `No plugin configuration found in server dataStore`                                         | The entry isn't at `dhis2AndroidPlugins/config`, or the logged-in user can't read that namespace. Verify with `curl -u <user:pass> "https://<server>/api/dataStore/dhis2AndroidPlugins/config"` — if that returns the JSON but the app disagrees, check the key's sharing settings for the user you logged in as (§4.2). Zero plugins is a normal outcome, not an error: the app logs it and carries on. |                                                                                                                                                                                    |
| `Could not refresh plugin configuration — using cached config`                              | The device couldn't reach the server to refresh the namespace. Expected when offline, and harmless — the app falls back to the config cached from a previous run. Only a problem on a first run, where there is no cache yet and no plugins will load.                                                                                                                                                   |                                                                                                                                                                                    |
| `Response from … is not a zip bundle: N bytes, content-type=text/html`                      | The `downloadUrl` answers with HTML under a 200 status — almost always because nothing is serving the bundle on that port and something else answered. Check for a preceding `Plugin download redirected` warning, then `curl -sI <url>` from the host. See §8 step 3.                                                                                                                                   |                                                                                                                                                                                    |
| `Plugin download redirected: … -> …/login/`                                                 | The `downloadUrl` port is owned by another service (typically a local DHIS2 instance on `8080`) that redirects to its own login page. Serve the bundle on a free port and update `downloadUrl`. Redirects themselves are fine — App Hub URLs legitimately point at a CDN — so this is a warning, not an error.                                                                                           |                                                                                                                                                                                    |
| `Too many redirects (> 5) downloading plugin from …`                                        | Redirect loop at the hosting end. Resolve the final URL by hand (`curl -sIL <url>`) and use it directly.                                                                                                                                                                                                                                                                                                 |                                                                                                                                                                                    |
| `HTTP 4xx/5xx when downloading plugin from …`                                               | Wrong filename, wrong port, or the static server isn't running. The served filename just has to match `downloadUrl` exactly — it is not required to encode the id or version.                                                                                                                                                                                                                            |                                                                                                                                                                                    |
| `Plugin checksum mismatch!`                                                                 | The served zip doesn't match `checksum` in the config. Confirm what is actually served — `curl -s <downloadUrl> \                                                                                                                                                                                                                                                                                        | shasum -a 256` from the host — then update the JSON (with the `sha256:` prefix). If the served bytes aren't a zip at all you'll get the `is not a zip bundle` error above instead. |
| `Plugin bundle signature verification failed` / `Unsigned entry in plugin bundle`           | The zip was edited after signing. Re-run `:plugin:buildPluginBundle`; never hand-edit the zip.                                                                                                                                                                                                                                                                                                           |                                                                                                                                                                                    |
| `ClassCastException: … not assignable to Dhis2Plugin`                                       | Plugin DEX bundles its own SDK copy. Keep `plugin-sdk` + all `compose.*` deps (except `compose.components.resources`) as `compileOnly`.                                                                                                                                                                                                                                                                  |                                                                                                                                                                                    |
| `NoSuchMethodError` for mangled `Text`/`Card` signatures                                    | Compose ABI mismatch. Plugin is compiled against CMP 1.10.3; consumer is on a different version. A preview app and the Capture App must both use CMP (`compose.runtime` etc.), not `androidx.compose.bom`.                                                                                                                                                                                               |                                                                                                                                                                                    |
| `MissingResourceException` for `composeResources/…`                                         | Capture App: `PluginSlot` should provide `LocalResourceReader` per-plugin. Preview app: the plugin's `composeResources` must be staged into its assets.                                                                                                                                                                                                                                                  |                                                                                                                                                                                    |
| Plugin code changes aren't visible                                                          | Cached bundle. Bump the plugin's `version` (and the `version` in the dataStore JSON) or `adb shell run-as com.dhis2.debug rm -rf files/plugins`.                                                                                                                                                                                                                                                         |                                                                                                                                                                                    |
| `Plugin system requires API 26+`                                                            | Device/emulator is API < 26. Use an API 26+ image.                                                                                                                                                                                                                                                                                                                                                       |                                                                                                                                                                                    |
| `This plugin project is not compatible with the DHIS2 Capture App host` (at configure time) | A §5.1 check failed. The message lists every mismatch and what each would have caused; fix the versions rather than setting `verifyToolchain = false`. `--info` prints what was detected.                                                                                                                                                                                                                |                                                                                                                                                                                    |
| `Plugin 'org.dhis2.mobile.plugin-bundle' not found` (at configure time)                     | The Gradle plugin isn't in Maven Local, or `mavenLocal()` is missing from the plugin project's `pluginManagement.repositories`. See §8 step 1.                                                                                                                                                                                                                                                           |                                                                                                                                                                                    |
| `This module packages classes the host already owns`                                        | Build-time form of the `ClassCastException` above, caught by inspecting the AAR before dexing. Move the listed dependency to `compileOnly`.                                                                                                                                                                                                                                                              |                                                                                                                                                                                    |
| `No Android build-tools installed under …` / `d8 not found` / `apksigner not found`         | Install build-tools through the SDK Manager, or set `pluginBundle.d8Executable` / `pluginBundle.apksignerExecutable`.                                                                                                                                                                                                                                                                                    |                                                                                                                                                                                    |
| `Signing keystore not found at …`                                                           | No `~/.android/debug.keystore` on this machine (install Android Studio, or create one with `keytool`), or configure `pluginBundle.signing`.                                                                                                                                                                                                                                                              |                                                                                                                                                                                    |

---

*Source files for reference:*

- `plugin-sdk/src/commonMain/kotlin/org/dhis2/mobile/plugin/sdk/` — `PluginMetadata.kt`,
  `InjectionPoint.kt`
- `plugin-sdk/src/androidMain/kotlin/org/dhis2/mobile/plugin/sdk/` — `Dhis2Plugin.kt`,
  `Dhis2PluginContext.kt` (androidMain because `D2` is Android-only; see §3)
- `plugin-sdk-gradle/src/main/kotlin/org/dhis2/mobile/plugin/gradle/` — `PluginBundlePlugin.kt`,
  `PluginBundleExtension.kt`, `BuildPluginBundleTask.kt`, `ToolchainPreflight.kt`,
  `DeterministicZip.kt`, `ClassesJarInspector.kt`, `DataStoreSnippet.kt`, `AndroidPluginWiring.kt`,
  `AndroidSdkTools.kt`, `SigningSpec.kt`
- `plugin/src/main/java/org/dhis2/mobile/plugin/` — `data/AppHubPluginRepository.kt`,
  `data/PluginDownloader.kt`, `data/PluginVerifier.kt`, `data/PluginLoader.kt`,
  `domain/LoadPluginsUseCase.kt`, `registry/PluginRegistry.kt`,
  `security/HostDhis2PluginContext.kt`, `di/PluginContainer.kt`, `di/PluginModule.kt`,
  `ui/PluginSlot.kt`, `ui/FileSystemResourceReader.kt`
