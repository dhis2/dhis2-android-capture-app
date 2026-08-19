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
  that runs inside the Capture App under a scope-enforced SDK.

The host app does download, integrity + signature verification, class-loader filtering, DI
isolation, resource injection, and rendering. The plugin only provides a Composable — its identity
and data scope live in the server config.

It is worth being precise about "sandboxing", which this is not: plugins run in the host process.
§6 says what the boundary does and does not achieve.

## 2. How it works

```
Developer      :plugin:buildPluginBundle  →  signed zip {module}-{version}.zip
Developer      uploads zip to a URL reachable by the device
DHIS2 admin    writes JSON to dataStore dhis2AndroidPlugins/config
Capture App    on opening Home:  refresh dhis2AndroidPlugins namespace
                        → read config → download → SHA-256 → JAR signature
                        → extract zip (path-traversal checked)
                        → load DEX via InMemoryDexClassLoader, parented by
                          FilteringClassLoader
                        → build ScopedD2 from the granted scope
                        → build a private Koin container for the plugin's module
                        → register instance
Capture App    at render: PluginSlot(slot) per plugin
                        → FileSystemResourceReader via LocalResourceReader
                        → LocalContext wrapped to return the plugin's loader
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
cases, etc.).

These bindings go into a **private container for that plugin**, not the host's. Nothing from the
host is seeded into it, so `get<D2>()` will not resolve — data access goes through
`Dhis2PluginContext.sdk`. `koinInject()` and `koinViewModel()` inside the plugin's Composables
resolve against that container.

### PluginMetadata

Describes a plugin's identity, version, data scope, and distribution metadata.

**Server-owned.** The DHIS2 administrator authors this as JSON in the server dataStore
(namespace `dhis2AndroidPlugins`, key `config`); it is the single source of truth. Plugins do
not declare any of it — the host reads it to decide what to download, verify and load, then
hands it to the plugin through `[Dhis2PluginContext.pluginMetadata]`. In particular `[scope]` is
*granted* by the server, so a plugin cannot widen its own access.

```kotlin
@Serializable
data class PluginMetadata(
    val id: String,                                 // "org.myorg.my-plugin"
    val version: String,                            // "1.0.0"
    val entryPoint: String,                         // "org.myorg.plugin.MyPlugin"
    val scope: PluginScope? = null,                 // what the plugin may read and write
    val injectionPoints: List<InjectionPoint> = emptyList(),
    val downloadUrl: String = "",
    val checksum: String = "",                      // "sha256:<hex>"
    // Superseded by `scope`; kept so existing configs keep working. See §4.
    val allowedProgramUids: List<String> = emptyList(),
    val allowedDataSetUids: List<String> = emptyList(),
)
```

### PluginScope

Every dimension is closed by default, so an omitted `scope` grants nothing. Read access and write
access are separate, and `writable` is always intersected with the read grant — listing something
there that is not readable has no effect.

```kotlin
@Serializable
data class PluginScope(
    val programs: UidGrant = UidGrant.NONE,           // {"uids": [...]} or {"all": true}
    val dataSets: UidGrant = UidGrant.NONE,
    val trackedEntityTypes: UidGrant = UidGrant.ALL,
    val dataElements: UidGrant = UidGrant.ALL,
    val orgUnits: OrgUnitGrant = OrgUnitGrant.NONE,   // + "mode": SELECTED | CHILDREN | DESCENDANTS
    val writable: WritableGrant = WritableGrant(),
    val capabilities: List<String> = emptyList(),     // PluginCapability names
)
```

Capabilities are opt-in feature areas — `READ_METADATA`, `READ_TRACKED_ENTITY`, `READ_ENROLLMENT`,
`READ_EVENT`, `READ_DATA_VALUE`, `SEARCH_TRACKED_ENTITY`, and the `WRITE_*` counterparts. An empty
list exposes nothing regardless of which UIDs are granted. A capability name the app does not
recognise is ignored with a warning rather than failing the plugin, so a config written for a newer
app still loads on an older one — with less access, never more.

### Injection points
Named slots in the host app where a plugin's Composable UI can be rendered.

A plugin declares the slots it targets in [PluginMetadata.injectionPoints].
The host app renders registered plugins at each slot via `PluginSlot`.

In the future we can create as many injections point in the app as we need.

```kotlin
enum class InjectionPoint {
    /** Rendered on the home screen, immediately above the program list. */
    HOME_ABOVE_PROGRAM_LIST,
}
```

### Plugin context

The gateway through which a plugin reaches DHIS2 data.

```kotlin
interface Dhis2PluginContext {
    val pluginMetadata: PluginMetadata
    val sdk: ScopedD2
}
```

`sdk` is **the DHIS2 Android SDK itself**, narrowed to the granted scope. The repositories it hands
back are ordinary SDK repositories, so a plugin gets the whole fluent API — filters, ordering,
paging, children, `blockingGet()` — at full granularity, with no wrapper in the way:

```kotlin
val overdue = context.sdk.events()
    .byStatus().eq(EventStatus.OVERDUE)
    .byOrganisationUnitUid().eq(clinicUid)
    .orderByDueDate(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()
```

**Why the grant cannot be widened.** `RepositoryScope` filters in the SDK are append-only and
copy-on-write: every `by*()` routes through `RepositoryScopeHelper`, which only ever does
`filters + item`, and the scope itself is `protected`. No API removes, replaces or resets a filter.
So a repository that arrives carrying `byProgramUid().in(granted)` can only be narrowed further —
asking for a program outside the grant returns nothing, not something wider. This is enforcement by
construction rather than by check, which is why it costs nothing at query time.

Writes get no protection from filters, because a create projection or value object carries its own
org unit, program and data element regardless of the query. They are covered separately by a guard
carried on the same scope and consulted at every write entry point, which validates the object
actually being written. A refused write throws `D2Error` with `D2ErrorCode.SCOPE_VIOLATION`.

`ScopedD2` is created by `D2.scopedTo(scope)` — a generic SDK feature, not a plugin-specific one.
See its KDoc for the full accessor list and for what is deliberately withheld: `databaseAdapter()`,
`httpServiceClient()`, `wipeModule()`, `dataStoreModule()` (which holds the plugin config itself, so
exposing it would let a plugin grant itself more), `userModule()`, and the sync/transport modules.
Analytics and relationships are deferred — both can reach outside a grant and need their own design
pass.

Contract:

- A plugin declares **no identity of its own** — no id, version, entry point or
  data scope. All of it comes from the server config, so there is exactly one
  place to change it and a plugin cannot widen its own access. The one apparent
  exception is cosmetic: `pluginBundle { pluginId; entryPoint }` (§5.3) fills in the
  generated `plugin-config.json` and reaches nothing else — not the bundle, not the
  host, which reads both from the dataStore.
- The entry-point class must be **public** with a **no-arg constructor** — the
  host instantiates it via reflection.
- `content()` runs inside the host composition; don't navigate outside the slot.
- `Dhis2Plugin` and `Dhis2PluginContext` live in `plugin-sdk`'s **androidMain**, because `ScopedD2`
  is the Android SDK. Write your plugin class in `src/androidMain`; `PluginMetadata`,
  `PluginScope` and `InjectionPoint` stay in `commonMain`.

The three DTO methods (`getTrackedEntityInstances`, `getDataValues`, `saveDataValue`) still exist,
deprecated, so plugins written against the previous API keep working. They now run through the same
scoped repositories. One behaviour change worth knowing: `saveDataValue` used to check the
`dataSetUid` *argument* and then write whichever data element the caller passed, so a plugin granted
one data set could write any data element in the database. The write is now checked against the
value actually being written.

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
      "injectionPoints": [
        "HOME_ABOVE_PROGRAM_LIST"
      ],
      "scope": {
        "programs": { "uids": ["IpHINAT79UW"] },
        "orgUnits": { "uids": ["O6uvpzGd5pu"], "mode": "DESCENDANTS" },
        "capabilities": [
          "READ_METADATA",
          "READ_TRACKED_ENTITY",
          "READ_EVENT"
        ],
        "writable": {
          "dataSets": { "uids": [] }
        }
      }
    }
  ]
}
```

Reading this scope: the plugin may read metadata, tracked entities and events, for one program,
within one org unit and everything beneath it, and may write nothing. Anything not named is not
granted — there is no implicit access.

`mode` is `SELECTED`, `CHILDREN` or `DESCENDANTS`. `ACCESSIBLE` and `ALL` are deliberately not
honoured: they resolve against the logged-in user rather than the org units the config named, so
accepting them would let a config reach past the units it listed. They fall back to `DESCENDANTS`
with a warning.

**Existing configs keep working.** A config with no `scope` block is read as the grant that
reproduces what it used to get — read access across `allowedProgramUids`, read and write on
`allowedDataSetUids`, and no org unit restriction, because there was none. Both fields are
deprecated; new configs should use `scope` and get closed-by-default everywhere.

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

The plugin class goes in **`androidMain`**, not `commonMain`: `Dhis2PluginContext.sdk` is the DHIS2
Android SDK, which has no common-source equivalent. Compose resources stay in `commonMain`, where
the resource generator expects them — the generated `Res` class is visible from `androidMain`.

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
        // *resolution* with an error that never mentions the DHIS2 SDK.
        maven("https://jitpack.io")
        // Only while the scoped-access work is unreleased.
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
        val events by produceState(emptyList<Event>()) {
            value = context.sdk.events()
                .byStatus().eq(EventStatus.OVERDUE)
                .orderByDueDate(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
        }
        Text(stringResource(Res.string.plugin_title, events.size))
    }
}
```

That is the whole plugin. The id, version, entry-point class name, injection points and data scope
are the server admin's to declare (§4) — the plugin restates none of them, and reads them back from
`context.pluginMetadata` if it needs them.

`context.sdk` gives the SDK's full query API within the granted scope. Narrowing further is always
safe; asking for something outside the grant returns nothing rather than failing, because the grant
and your filter are AND-ed. If you need to know what you were granted, read
`context.pluginMetadata.scope` rather than guessing from an empty result.

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
in one postable file; the dataStore is still the only thing the host reads identity from, and
`allowedProgramUids` / `allowedDataSetUids` remain the administrator's to grant.

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

**Read this first: plugins run in the host process, so this is a guardrail, not a sandbox.** Nothing
below prevents a determined author from reaching data they were not granted. What it does is make
the sanctioned path fully capable, make every other path deliberate rather than accidental, and make
an attempt to leave the path visible in code review. Choosing which plugins to run is still the
security decision that matters.

**Scope enforcement (SDK).**
- Reads: repositories arrive pre-narrowed to the grant. SDK filters are append-only and
  copy-on-write with no removal API, so a plugin's own `by*()` calls can only narrow further. An
  out-of-scope query returns empty, not wider results.
- Writes: a guard travels on the same scope and validates the object being written at every write
  entry point — `add()`, `set()`, `delete()`, `dataValues().value(…)`. Refusal is
  `D2Error(SCOPE_VIOLATION)`, which the plugin API restates as `SecurityException`.
- Tracker search uses the SDK's other scope mechanism, whose fields are *replaced* rather than
  appended, so the grant is re-applied on every repository the fluent API produces, and the online
  modes are forced offline — a server-side search is answered where none of these restrictions
  exist.
- Unknown model types are refused by default, so a type added to the SDK later cannot become
  silently writable.

**Containment of the escape routes.**
- Plugins load through a filtering class loader that refuses `D2`, `D2Manager`, the SDK's `arch.*`
  plumbing, `dataStoreModule`, `wipeModule`, `userModule`, Koin's `GlobalContext`, and the host's
  own `org.dhis2.*` classes. `LocalContext` is wrapped so `LocalContext.current.classLoader` returns
  that loader too. Still open by construction: `Thread.currentThread().contextClassLoader`,
  `ClassLoader.getSystemClassLoader()`, the loader of any host object the plugin holds, and
  reflection — Kotlin `internal` is public in JVM bytecode.
- Each plugin gets a **private Koin container**, seeded with nothing from the host. Loading plugin
  modules into the application container previously let a plugin resolve every host binding
  including `D2`, and — since Koin allows override by default — silently *replace* them.
- The bundle build fails on references to denied classes, and on packaging any class the host owns
  (`plugin-sdk`, Compose, Kotlin, the DHIS2 SDK, Koin). Static analysis, so a name assembled at
  runtime passes; the value is a clear build error for the honest mistake.
- Bundle entries are checked for path traversal before extraction, as are plugin resource reads. A
  valid signature attests to *who* built a zip, not to what is inside it.

**Provenance.**
- **Integrity.** SHA-256 verified before load. Mismatch evicts the cache.
- **Authorship.** JAR signature (v1 scheme) verified with `JarFile(bundle, verify = true)`; every
  entry must be covered. Any valid signature passes today — per-publisher cert allow-listing is
  future work.
- **Config write access.** The dataStore key names the code the app will execute, and DHIS2 creates
  dataStore keys publicly writable. Whoever can write that key can add a plugin to every device on
  the instance, so restrict it to administrators (§4.2).

**Process.**
- **API guard.** `InMemoryDexClassLoader` requires API 26+; older devices skip
  the whole plugin system (log + empty registry).
- Plugins run **in-process**. A plugin that throws while composing takes the enclosing screen with
  it, and this cannot be fixed with an error boundary: Compose rejects `try`/`catch` around a
  composable invocation, because recomposition has no way to unwind a partially-applied composition.
  Real containment needs a separate process; the grant and the enforcement live in the SDK precisely
  so a future IPC layer can sit in front of the same `ScopedD2` rather than redoing it.

## 7. Current limitations

- One injection point: `HOME_ABOVE_PROGRAM_LIST`. Mora can be added in the future.
- The plugin-bundle Gradle plugin is published to Maven Local only — not yet to Maven Central or
  the Gradle Plugin Portal, so plugin authors need `mavenLocal()` in `pluginManagement`.
- No plugin uninstall flow — delete the dataStore entry (§4.1) and the device cache
  (`/data/data/com.dhis2.debug/files/plugins/{id}-{version}.zip` — the cache is named from
  the config's id/version, not the served filename).
- No per-publisher cert allow-list.
- Analytics and relationships are not exposed by `ScopedD2` yet — both can reach outside a grant
  (a free-form dimension DSL, and object-graph traversal to tracked entities in other programs) and
  need their own design pass.
- `TrackedEntityInstanceQueryRepositoryScope` still has a public constructor, so a grant installed on
  it can in principle be bypassed by constructing a scope directly rather than through the builder.
  Making that constructor `internal` is a public-API break and needs a deprecation cycle.
- Plugins are Android-only. `plugin-sdk` still publishes a desktop artifact, but it carries only the
  portable config types — `Dhis2Plugin` and `Dhis2PluginContext` are in `androidMain` because
  `ScopedD2` is the Android SDK.
- Plugin authors are pinned to the host's DHIS2 SDK version, since the plugin ABI now includes the
  SDK's. The bundle plugin injects the right version so it cannot drift silently.
- **No `plugin-sdk-test` artefact, and previewing is now harder than it was.** `ScopedD2` has an
  internal constructor, so a hand-written `Dhis2PluginContext` can no longer stub the data source the
  way it could when the context returned DTOs — a preview app would need a real `D2` instance to call
  `scopedTo` on. This is the direct cost of exposing real SDK types instead of a facade, and it
  makes the §8 preview workflow (and the `StubDhis2PluginContext` the sample plugin uses) need
  rework. The fix belongs in the SDK: either a `@VisibleForTesting` constructor on `ScopedD2` or a
  documented in-memory `D2` for tests, exposed through a `plugin-sdk-test` artefact.

## 8. Testing a plugin locally

What you need: this repo, your plugin project (§5), an API 26+ emulator or device, a DHIS2 server
you can write a dataStore namespace on, and a local static file server.

1. **Publish the DHIS2 SDK**, while the scoped-access work is unreleased — in the SDK repo:

   ```bash
   ./gradlew :core:publishToMavenLocal
   ```

   A plugin now compiles against `org.hisp.dhis:android-core`, so it needs the same build the host
   runs. Skip this and the plugin project fails to resolve `ScopedD2`.

2. **Publish the plugin SDK and its Gradle plugin to Maven Local** — in *this* repo:

   ```bash
   ./gradlew :plugin-sdk:publishToMavenLocal :plugin-sdk-gradle:publishToMavenLocal
   ```

   Both, always: the `id("org.dhis2.mobile.plugin-bundle")` line resolves from Maven Local, and it
   is what pulls in the matching `plugin-sdk` *and* the matching `android-core`. Your plugin project
   needs `mavenLocal()` in both its `pluginManagement` and `dependencyResolutionManagement`
   repositories, plus JitPack (§5.1).

3. **Build the bundle** — in your plugin project:

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

4. **Serve it to the emulator.** Pick a port that nothing else is using — a local
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

5. **Point the Capture App at the bundle.**

   Write the JSON (§4) to the DHIS2 server dataStore. `plugin-config.json` next to the bundle is
   that JSON with `version` and `checksum` already filled in, plus `id` and `entryPoint` if you
   declared them (§5.3) — fill in whatever is left, `downloadUrl` and the granted scope, then
   post it:

   ```bash
   # first time — POST creates the key; use PUT to update it afterwards
   curl -u <user>:<pass> -H 'Content-Type: application/json' \
     -d @plugin-config.json \
     "https://<server>/api/dataStore/dhis2AndroidPlugins/config"
   ```

   The dataStore is the only source of plugin config; there is no in-app fallback. If the app logs
   `No plugin configuration found in server dataStore`, see §9.

6. **Run the Capture App** and log in — from this repo:

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

7. **Iterate.** After code changes: bump `version` in the plugin's
   `build.gradle.kts` and the matching `version` in the dataStore JSON, rebuild,
   restart the app. Or wipe the device cache:

   ```bash
   adb shell run-as com.dhis2.debug rm -rf files/plugins
   ```

8. **Locale test.** Switch the emulator language (Settings → Languages) and
   reopen the screen. The plugin's strings should change accordingly.

### Previewing without the Capture App (optional)

The loop above is slow for UI work. You can add an Android application module to *your own* plugin
project that instantiates the plugin class directly, passing a hand-written `Dhis2PluginContext`.

> **This got harder.** `Dhis2PluginContext.sdk` is a `ScopedD2`, which has an internal constructor,
> so you cannot fabricate one the way you could fabricate the old DTOs. A preview harness now needs a
> real `D2` to call `scopedTo` on, or the plugin has to keep its Composables free of data access and
> take the data as parameters — which is the more testable shape anyway. See §7; a `plugin-sdk-test`
> artefact is the proper fix and does not exist yet.

Two more things that trip this up:

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
| `Response from … is not a zip bundle: N bytes, content-type=text/html`                      | The `downloadUrl` answers with HTML under a 200 status — almost always because nothing is serving the bundle on that port and something else answered. Check for a preceding `Plugin download redirected` warning, then `curl -sI <url>` from the host. See §8 step 4.                                                                                                                                   |                                                                                                                                                                                    |
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
| `Plugin 'org.dhis2.mobile.plugin-bundle' not found` (at configure time)                     | The Gradle plugin isn't in Maven Local, or `mavenLocal()` is missing from the plugin project's `pluginManagement.repositories`. See §8 step 2.                                                                                                                                                                                                                                                           |                                                                                                                                                                                    |
| `Could not find com.github.dhis2:sms-compression`                                           | JitPack is missing from `dependencyResolutionManagement.repositories`. It is a transitive dependency of the DHIS2 SDK, which a plugin now compiles against — the error names the transitive artifact, not the SDK. See §5.1.                                                                                                                                                                              |                                                                                                                                                                                    |
| `This module packages classes the host already owns`                                        | Build-time form of the `ClassCastException` above, caught by inspecting the AAR before dexing. Move the listed dependency to `compileOnly`. The list now includes `org/hisp/dhis/` and `org/koin/` — a plugin must never carry its own copy of the SDK or Koin.                                                                                                                                            |                                                                                                                                                                                    |
| `This plugin references classes the DHIS2 Capture App does not expose to plugins`            | The plugin names something the host's class loader refuses — usually `D2Manager`, `GlobalContext`, or a host `org.dhis2.*` class. Data access goes through `context.sdk`; there is no supported route to an unrestricted `D2`. The message lists the referencing class and what it referenced.                                                                                                             |                                                                                                                                                                                    |
| `ClassNotFoundException: Class '…' is not available to plugins` (at runtime)                 | The same rule, hit at load time rather than build time — typically because the name was assembled at runtime and the static check could not see it.                                                                                                                                                                                                                                                       |                                                                                                                                                                                    |
| `D2Error … SCOPE_VIOLATION` / `SecurityException` on a write                                 | The write fell outside the granted scope. Unlike the query path, writes are checked against the object being written: its org unit, program or data element. Check the `scope.writable` block in the dataStore config — it is intersected with the read grant, so a data set that is writable but not readable grants nothing.                                                                             |                                                                                                                                                                                    |
| A scoped query returns nothing when you expected rows                                       | The grant and your filter are AND-ed, so filtering for something outside the grant yields empty rather than an error. Read `context.pluginMetadata.scope` to see what was actually granted. Note also that `capabilities` is opt-in — an accessor whose capability is missing throws rather than returning empty.                                                                                          |                                                                                                                                                                                    |
| `No Android build-tools installed under …` / `d8 not found` / `apksigner not found`         | Install build-tools through the SDK Manager, or set `pluginBundle.d8Executable` / `pluginBundle.apksignerExecutable`.                                                                                                                                                                                                                                                                                    |                                                                                                                                                                                    |
| `Signing keystore not found at …`                                                           | No `~/.android/debug.keystore` on this machine (install Android Studio, or create one with `keytool`), or configure `pluginBundle.signing`.                                                                                                                                                                                                                                                              |                                                                                                                                                                                    |

---

*Source files for reference:*

- `plugin-sdk/src/commonMain/kotlin/org/dhis2/mobile/plugin/sdk/` — `PluginMetadata.kt`,
  `PluginScope.kt`, `InjectionPoint.kt`
- `plugin-sdk/src/androidMain/kotlin/org/dhis2/mobile/plugin/sdk/` — `Dhis2Plugin.kt`,
  `Dhis2PluginContext.kt`, `dto/*`
- DHIS2 Android SDK, `core/src/main/java/org/hisp/dhis/android/core/scopedaccess/` — `ScopedD2.kt`,
  `D2DataScope.kt`, `UidScope.kt`, `OrgUnitScope.kt`, `D2Capability.kt`,
  `internal/{ScopeResolver,ScopedAccessGuard}.kt`; plus `arch/repositories/scope/internal/AccessGuard.kt`
  and `trackedentity/search/TrackedEntityQueryGrant.kt`
- `plugin-sdk-gradle/src/main/kotlin/org/dhis2/mobile/plugin/gradle/` — `PluginBundlePlugin.kt`,
  `PluginBundleExtension.kt`, `BuildPluginBundleTask.kt`, `ToolchainPreflight.kt`,
  `DeterministicZip.kt`, `ClassesJarInspector.kt`, `DataStoreSnippet.kt`, `AndroidPluginWiring.kt`,
  `AndroidSdkTools.kt`
- `plugin/src/main/java/org/dhis2/mobile/plugin/` — `data/AppHubPluginRepository.kt`,
  `data/PluginDownloader.kt`, `data/PluginVerifier.kt`, `data/PluginLoader.kt`,
  `domain/LoadPluginsUseCase.kt`, `registry/PluginRegistry.kt`,
  `security/ScopedDhis2PluginContext.kt`, `ui/PluginSlot.kt`, `ui/FileSystemResourceReader.kt`
