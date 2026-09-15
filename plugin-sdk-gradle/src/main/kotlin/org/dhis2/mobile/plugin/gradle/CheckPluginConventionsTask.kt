package org.dhis2.mobile.plugin.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Fails the build when a plugin project breaks one of [PluginConventions].
 *
 * A task rather than a configuration-time check like [ToolchainPreflight], because three of the four
 * rules read Kotlin source: doing that in `afterEvaluate` would run on every `./gradlew tasks`, get
 * no up-to-date checking, and do file I/O at configuration time, which is what the configuration
 * cache exists to stop. Dependencies are captured as plain strings at configuration time for the
 * same reason — a `Configuration` is not something a task may hold and still be cacheable.
 */
abstract class CheckPluginConventionsTask : DefaultTask() {

    /** Kotlin source roots, tagged with their source-set name as `<name>|<absolute path>`. */
    @get:Input
    abstract val sourceRoots: ListProperty<String>

    /**
     * The files themselves, so Gradle can tell whether anything changed.
     *
     * Separate from [sourceRoots] because that carries the source-set names — which decide whether
     * a file counts as shared — while this carries the up-to-date check.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    /** Declared dependencies as `<sourceSet>|<bucket>|<group>:<name>`. */
    @get:Input
    abstract val declaredDependencies: ListProperty<String>

    /** Only for making reported paths relative, so the error reads like a compiler's. */
    @get:Input
    abstract val projectDirectory: org.gradle.api.provider.Property<String>

    @TaskAction
    fun check() {
        val root = File(projectDirectory.get())
        val files = sourceRoots.get().flatMap { entry ->
            val (sourceSet, path) = entry.split('|', limit = 2)
            File(path)
                .walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .map { file ->
                    PluginConventions.SourceFile(
                        path = file.relativeToOrSelf(root).path,
                        sourceSet = sourceSet,
                        text = file.readText(),
                    )
                }
                .toList()
        }

        val dependencies = declaredDependencies.get().mapNotNull { entry ->
            val parts = entry.split('|', limit = 3)
            if (parts.size < 3) return@mapNotNull null
            PluginConventions.DeclaredDependency(
                sourceSet = parts[0],
                bucket = parts[1],
                coordinates = parts[2],
            )
        }

        val violations = PluginConventions.violations(files, dependencies)
        if (violations.isEmpty()) {
            logger.info(
                "plugin conventions OK — ${files.size} source file(s), " +
                    "${dependencies.size} declared dependency(ies)",
            )
            return
        }

        throw GradleException(
            "This plugin project breaks conventions the DHIS2 Capture App relies on:\n" +
                violations.joinToString("\n") { "  - $it" } +
                "\n\nEach of these fails on a device rather than at build time, which is why it is " +
                "checked here. See §5.5 of the DHIS2 plugin system documentation. Set " +
                "pluginBundle.verifyConventions = false to skip these checks.",
        )
    }
}
