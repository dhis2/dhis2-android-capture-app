package org.dhis2.mobile.plugin.gradle

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import java.io.File

/**
 * Everything that touches the Android and Kotlin Gradle plugins.
 *
 * Kept out of [PluginBundlePlugin] on purpose: Gradle decorates a plugin class on instantiation and
 * has to load every type in its method signatures, so mentioning AGP types there would make
 * applying this plugin to a project without AGP fail with `NoClassDefFoundError` instead of quietly
 * doing nothing. Members of this object are only reached from inside a `withPlugin` guard, where
 * AGP and KGP are definitionally present.
 */
internal object AndroidPluginWiring {
    /**
     * Adds the API a plugin implements. `compileOnly` on purpose: at runtime these classes come from
     * the host's class loader, and a second copy inside the plugin DEX fails with
     * `ClassCastException: … not assignable to Dhis2Plugin`.
     */
    fun addPluginSdkDependency(project: Project) {
        val kotlin = project.extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return
        kotlin.sourceSets.named("commonMain").configure { sourceSet ->
            sourceSet.dependencies {
                compileOnly("org.dhis2.mobile:plugin-sdk:${HostToolchain.PLUGIN_SDK_VERSION}")
            }
        }
        // The plugin API exposes D2, so a plugin compiles against the DHIS2 SDK — and must compile
        // against exactly the version the host runs, or the DEX resolves methods that are not there.
        // Injected rather than declared by the plugin author, so it cannot drift silently.
        // androidMain only: D2 is the Android SDK and has no common-source equivalent.
        kotlin.sourceSets.named("androidMain").configure { sourceSet ->
            sourceSet.dependencies {
                compileOnly("org.hisp.dhis:android-core:${HostToolchain.DHIS2_SDK_VERSION}")
            }
        }
    }

    /**
     * Puts the compile-only dependencies on the JVM test runtime classpath.
     *
     * `plugin-sdk` and `android-core` are `compileOnly` because the host supplies them through its
     * class loader — so they are on the compile classpath and *not* the runtime one. A JVM test that
     * builds a real `D2Error`, or a `TrackedEntityInstance`, or touches any plugin-sdk type, then
     * dies with `NoClassDefFoundError` for a class that plainly compiled.
     *
     * No plugin author should have to work that out: this is the plugin system's own arrangement, so
     * undoing it for tests is the plugin system's job. `commonMainCompileOnly` matters as much as
     * `androidMainCompileOnly` — plugin-sdk is added to `commonMain`, which is what made this bite.
     *
     * Matched rather than named because the configuration only exists once the project opts into a
     * host-test target, and whether it has is not ours to assume.
     */
    fun wireHostTestRuntime(project: Project) {
        project.configurations
            .matching { it.name == HOST_TEST_RUNTIME_ONLY }
            .configureEach { hostTestRuntime ->
                COMPILE_ONLY_SOURCES.forEach { name ->
                    project.configurations.findByName(name)?.let(hostTestRuntime::extendsFrom)
                }
            }
    }

    /**
     * Points the bundle task at the AAR through AGP's artifacts API rather than at a guessed path
     * under `build/`, so the task dependency comes with it and it survives AGP moving its outputs.
     */
    fun wireAar(
        project: Project,
        bundleTask: TaskProvider<BuildPluginBundleTask>,
    ) {
        val components = androidComponents(project)
        components.onVariants(components.selector().all()) { variant ->
            bundleTask.configure { task -> task.aar.set(variant.artifacts.get(SingleArtifact.AAR)) }
        }
    }

    /** `d8` from the SDK location AGP resolved, falling back to the usual environment variables. */
    fun discoverD8(project: Project): Provider<RegularFile> = project.layout.file(sdkDirectory(project).map { AndroidSdkTools.d8(it) })

    /** `apksigner`, from the same build-tools installation as [discoverD8]. */
    fun discoverApksigner(project: Project): Provider<RegularFile> =
        project.layout.file(sdkDirectory(project).map { AndroidSdkTools.apksigner(it) })

    private fun sdkDirectory(project: Project): Provider<File> =
        project.extensions
            .findByType(KotlinMultiplatformAndroidComponentsExtension::class.java)
            ?.sdkComponents
            ?.sdkDirectory
            ?.map { it.asFile }
            ?: project.providers
                .environmentVariable("ANDROID_HOME")
                .orElse(project.providers.environmentVariable("ANDROID_SDK_ROOT"))
                .map { File(it) }

    /** What this project declares, for [ToolchainPreflight]. */
    fun toolchainState(project: Project): ToolchainState {
        val androidTarget =
            project.extensions
                .findByType(KotlinMultiplatformExtension::class.java)
                ?.targets
                ?.withType(KotlinMultiplatformAndroidLibraryTarget::class.java)
                ?.firstOrNull()

        return ToolchainState(
            kotlinVersion = project.getKotlinPluginVersion(),
            composeVersion = composeVersion(project),
            compileSdk = androidTarget?.compileSdk,
            minSdk = androidTarget?.minSdk,
            jvmTarget =
                androidTarget
                    ?.compilerOptions
                    ?.jvmTarget
                    ?.orNull
                    ?.target,
            usesLegacyAndroidLibraryPlugin = project.pluginManager.hasPlugin(LEGACY_ANDROID_LIBRARY_PLUGIN),
        )
    }

    /**
     * Kotlin source roots as `<sourceSetName>|<absolutePath>`, for the conventions task.
     *
     * Taken from the Kotlin extension rather than guessed from `src/`, so a project that moves or
     * adds a source set is still checked.
     */
    fun sourceRoots(project: Project): List<String> {
        val kotlin =
            project.extensions.findByType(KotlinMultiplatformExtension::class.java)
                ?: return emptyList()
        // Generated roots are excluded: they are another task's output, so reading them would make
        // this task depend on the Compose resource generator for no benefit — and generated code is
        // not the author's to fix. Everything checked here is something a human wrote.
        val buildDirectory =
            project.layout.buildDirectory
                .get()
                .asFile.absolutePath
        return kotlin.sourceSets.flatMap { sourceSet ->
            sourceSet.kotlin.srcDirs
                .map { dir -> dir.absolutePath }
                .filterNot { path -> path.startsWith(buildDirectory) }
                .map { path -> "${sourceSet.name}|$path" }
        }
    }

    /**
     * Every declared external dependency as `<sourceSet>|<bucket>|<group>:<name>`.
     *
     * Asking Gradle beats regexing the build script: it sees version-catalog aliases, multi-line
     * declarations, `sourceSets { commonMain { … } }` as well as `val commonMain by getting`, and
     * dependencies added by a convention plugin the author never wrote down.
     *
     * Project and file dependencies are skipped rather than guessed at.
     */
    fun declaredDependencies(project: Project): List<String> {
        val kotlin =
            project.extensions.findByType(KotlinMultiplatformExtension::class.java)
                ?: return emptyList()

        return kotlin.sourceSets
            .flatMap { sourceSet ->
                BUCKETS.flatMap { (bucket, configurationName) ->
                    val configuration = project.configurations.findByName(configurationName(sourceSet.name))
                    configuration
                        ?.dependencies
                        ?.filterIsInstance<ExternalModuleDependency>()
                        ?.map { dependency -> "${sourceSet.name}|$bucket|${dependency.group}:${dependency.name}" }
                        .orEmpty()
                }
            }.distinct()
    }

    /**
     * External dependencies declared on a source set's `api`/`implementation`. An AAR's `classes.jar`
     * holds only this module's own classes, so these are compiled against but never packaged.
     */
    fun notPackagedDependencies(project: Project): List<String> =
        project.configurations
            .filter { configuration ->
                configuration.name.endsWith("Implementation") || configuration.name.endsWith("Api")
            }.flatMap { configuration -> configuration.dependencies }
            .filterIsInstance<ExternalModuleDependency>()
            .map { dependency -> "${dependency.group}:${dependency.name}" }
            .filterNot { coordinates -> HOST_PROVIDED_GROUPS.any { coordinates.startsWith(it) } }
            .distinct()

    /**
     * Version the applied Compose plugin resolves its own artifacts at — the ABI a plugin compiles
     * against. Read reflectively because the constant is `const`: referencing it directly would
     * inline *our* version into this jar and check nothing.
     */
    private fun composeVersion(project: Project): String? {
        if (!project.pluginManager.hasPlugin(COMPOSE_PLUGIN)) return null
        return runCatching {
            Class
                .forName("org.jetbrains.compose.ComposeBuildConfig")
                .getField("composeVersion")
                .get(null) as? String
        }.getOrNull()
    }

    private fun androidComponents(project: Project): KotlinMultiplatformAndroidComponentsExtension =
        project.extensions.findByType(KotlinMultiplatformAndroidComponentsExtension::class.java)
            ?: throw GradleException(
                "The Android Gradle plugin did not register its Kotlin Multiplatform components " +
                    "extension, so the plugin AAR cannot be located.",
            )

    private const val LEGACY_ANDROID_LIBRARY_PLUGIN = "com.android.library"
    private const val COMPOSE_PLUGIN = "org.jetbrains.compose"

    private const val HOST_TEST_RUNTIME_ONLY = "androidHostTestRuntimeOnly"

    private val COMPILE_ONLY_SOURCES = listOf("commonMainCompileOnly", "androidMainCompileOnly")

    /**
     * The buckets a dependency can land in, and how a Kotlin source set names each configuration.
     *
     * `runtimeOnly` is included because a host-provided artifact declared there is packaged just the
     * same as one on `implementation`.
     */
    private val BUCKETS: List<Pair<String, (String) -> String>> =
        listOf(
            "api" to { name: String -> "${name}Api" },
            "implementation" to { name: String -> "${name}Implementation" },
            "compileOnly" to { name: String -> "${name}CompileOnly" },
            "runtimeOnly" to { name: String -> "${name}RuntimeOnly" },
        )

    /**
     * Groups the host is known to provide, so declaring them non-`compileOnly` is not worth a
     * warning. `compose.components.resources` in particular *must* be `implementation` — the Compose
     * Resources generator uses that declaration as its opt-in signal for the `Res` class.
     *
     * One list, two consumers: the conventions check errors on a host-provided dependency that is
     * not compileOnly, and notPackagedDependencies warns about its complement.
     */
    private val HOST_PROVIDED_GROUPS = PluginConventions.HOST_PROVIDED
}
