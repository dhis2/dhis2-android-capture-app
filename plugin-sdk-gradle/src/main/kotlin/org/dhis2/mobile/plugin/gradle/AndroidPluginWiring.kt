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

    /**
     * Groups the host is known to provide, so declaring them non-`compileOnly` is not worth a
     * warning. `compose.components.resources` in particular *must* be `implementation` — the Compose
     * Resources generator uses that declaration as its opt-in signal for the `Res` class.
     */
    private val HOST_PROVIDED_GROUPS =
        listOf(
            "org.jetbrains.compose",
            "org.jetbrains.kotlin",
            "org.dhis2.mobile:plugin-sdk",
        )
}
