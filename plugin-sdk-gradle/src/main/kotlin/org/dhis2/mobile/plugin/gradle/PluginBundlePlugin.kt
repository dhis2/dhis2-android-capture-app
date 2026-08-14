package org.dhis2.mobile.plugin.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * Packaging plugin for DHIS2 Android plugins: `id("org.dhis2.mobile.plugin-bundle")`.
 *
 * Applied to a Kotlin Multiplatform Android library it
 *
 * - registers `buildPluginBundle`, which produces the signed zip the Capture App downloads,
 * - adds `compileOnly("org.dhis2.mobile:plugin-sdk:…")` at the version this plugin ships with, so
 *   the API a plugin compiles against always matches the host that will load it and never has to be
 *   declared by hand, and
 * - checks the project's Kotlin, Compose, SDK and JVM-target settings against that host.
 *
 * Everything beyond the extension is wired inside `withPlugin` guards, so applying this to a project
 * that is not a Kotlin Multiplatform Android library does nothing rather than failing obscurely.
 */
class PluginBundlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("pluginBundle", PluginBundleExtension::class.java)
        target.applyConventions(extension)

        target.pluginManager.withPlugin(ANDROID_KMP_LIBRARY_PLUGIN) {
            AndroidPluginWiring.addPluginSdkDependency(target)
            val bundleTask = target.registerBundleTask(extension)
            AndroidPluginWiring.wireAar(target, bundleTask)
            target.wireComposeResources(bundleTask)
            target.afterEvaluate { project -> project.runPreflight(extension) }
        }
    }

    private fun Project.applyConventions(extension: PluginBundleExtension) {
        extension.minApi.convention(HostToolchain.MIN_SDK_FLOOR)
        extension.verifyToolchain.convention(true)
        extension.emitDataStoreSnippet.convention(true)
        extension.bundleFileName.convention(provider { "$name-$version.zip" })
        extension.outputDirectory.convention(layout.buildDirectory.dir("outputs/plugin-bundle"))
        extension.signing.alias.convention(DEBUG_KEY_ALIAS)
        extension.signing.storePassword.convention(DEBUG_KEY_PASSWORD)
        extension.signing.keyPassword.convention(DEBUG_KEY_PASSWORD)
        extension.signing.keystore.convention(
            layout.file(
                providers.systemProperty("user.home").map { home -> File(home, DEBUG_KEYSTORE) },
            ),
        )
    }

    private fun Project.registerBundleTask(extension: PluginBundleExtension): TaskProvider<BuildPluginBundleTask> =
        tasks.register(BUNDLE_TASK_NAME, BuildPluginBundleTask::class.java) { task ->
            task.group = "plugin"
            task.description = "Produces a signed zip bundle containing classes.dex + composeResources/."
            task.resourcePackage.set(extension.resourcePackage)
            task.minApi.set(extension.minApi)
            task.bundleFileName.set(extension.bundleFileName)
            task.pluginVersion.set(provider { version.toString() })
            task.emitDataStoreSnippet.set(extension.emitDataStoreSnippet)
            task.outputDirectory.set(extension.outputDirectory)
            task.signing.keystore.set(extension.signing.keystore)
            task.signing.alias.set(extension.signing.alias)
            task.signing.storePassword.set(extension.signing.storePassword)
            task.signing.keyPassword.set(extension.signing.keyPassword)
            task.d8Executable.set(extension.d8Executable.orElse(AndroidPluginWiring.discoverD8(this)))
            task.apksignerExecutable.set(
                extension.apksignerExecutable.orElse(AndroidPluginWiring.discoverApksigner(this)),
            )
        }

    /**
     * Compose resources are taken from the generator's own tasks, which both carries the task
     * dependency and keeps the staging layout in step with whatever path the generator writes to.
     *
     * Matched by name because the generator's task type is `internal` to the Compose plugin. The
     * collection is live, so it also picks up tasks registered after this plugin is applied.
     */
    private fun Project.wireComposeResources(bundleTask: TaskProvider<BuildPluginBundleTask>) {
        pluginManager.withPlugin(COMPOSE_PLUGIN) {
            val prepareTasks = tasks.matching { it.name.startsWith(PREPARE_RESOURCES_TASK_PREFIX) }
            bundleTask.configure { task -> task.preparedResources.from(prepareTasks) }
        }
    }

    private fun Project.runPreflight(extension: PluginBundleExtension) {
        if (!extension.verifyToolchain.get()) return

        val state = AndroidPluginWiring.toolchainState(this)
        // Logged so `--info` shows what was actually detected: a value this plugin cannot read is
        // skipped rather than failed on, and that is worth being able to see.
        logger.info("$path plugin toolchain: $state")

        val problems = ToolchainPreflight.problems(state)
        if (problems.isNotEmpty()) {
            throw GradleException(
                "This plugin project is not compatible with the DHIS2 Capture App host:\n" +
                    problems.joinToString("\n") { "  - $it" } +
                    "\n\nSee §5.1 of the DHIS2 plugin system documentation. Set " +
                    "pluginBundle.verifyToolchain = false to skip these checks.",
            )
        }

        ToolchainPreflight
            .runtimeDependencyWarning(AndroidPluginWiring.notPackagedDependencies(this))
            ?.let { logger.warn("$path: $it") }
    }

    private companion object {
        const val ANDROID_KMP_LIBRARY_PLUGIN = "com.android.kotlin.multiplatform.library"
        const val COMPOSE_PLUGIN = "org.jetbrains.compose"
        const val PREPARE_RESOURCES_TASK_PREFIX = "prepareComposeResourcesTaskFor"
        const val BUNDLE_TASK_NAME = "buildPluginBundle"
        const val DEBUG_KEYSTORE = ".android/debug.keystore"
        const val DEBUG_KEY_ALIAS = "androiddebugkey"
        const val DEBUG_KEY_PASSWORD = "android"
    }
}
