package org.dhis2.mobile.plugin.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
import javax.inject.Inject

/**
 * Packages this module as a signed DHIS2 plugin bundle.
 *
 * ```
 * {module}-{version}.zip
 * ├── META-INF/…              (apksigner, v1/JAR signature scheme)
 * └── android/
 *     ├── classes.dex         (this module's classes only — the host provides the rest)
 *     └── composeResources/{packageOfResClass}/…
 * ```
 *
 * The bundle carries no manifest of its own: the host reads a plugin's id, version, entry point and
 * data scope from the DHIS2 server dataStore, which is the single source of truth. The file name is
 * a convenience for whoever hosts the zip.
 *
 * The `android/` prefix leaves room for a future Desktop host: that means adding `desktop/plugin.jar`
 * beside it, not a second distribution format.
 */
@DisableCachingByDefault(because = "Signs with a local keystore; the signed bundle is not worth sharing between machines")
abstract class BuildPluginBundleTask
    @Inject
    constructor(
        private val execOperations: ExecOperations,
    ) : DefaultTask() {
        /** AAR produced by this module's Android target; its `classes.jar` becomes `classes.dex`. */
        @get:InputFile
        @get:PathSensitive(PathSensitivity.NONE)
        abstract val aar: RegularFileProperty

        /** Output of the Compose resource generator, laid out as the Compose runtime expects to read it. */
        @get:InputFiles
        @get:Optional
        @get:PathSensitive(PathSensitivity.RELATIVE)
        abstract val preparedResources: ConfigurableFileCollection

        /** Package resources are addressed by. Detected from the compiled `Res` class when absent. */
        @get:Input
        @get:Optional
        abstract val resourcePackage: Property<String>

        @get:Input
        abstract val minApi: Property<Int>

        @get:Input
        abstract val bundleFileName: Property<String>

        /** This module's `version`, used in the dataStore snippet. */
        @get:Input
        abstract val pluginVersion: Property<String>

        @get:Input
        abstract val emitDataStoreSnippet: Property<Boolean>

        /** `id` written into the dataStore snippet. Configured through `pluginBundle.pluginId`. */
        @get:Input
        abstract val pluginId: Property<String>

        /** `entryPoint` written into the dataStore snippet, likewise configured on the extension. */
        @get:Input
        abstract val entryPoint: Property<String>

        @get:Nested
        abstract val signing: SigningSpec

        /**
         * Tool locations are deliberately [Internal]: they are machine-specific paths, not content that
         * should take part in up-to-date checks.
         */
        @get:Internal
        abstract val d8Executable: RegularFileProperty

        @get:Internal
        abstract val apksignerExecutable: RegularFileProperty

        @get:OutputDirectory
        abstract val outputDirectory: DirectoryProperty

        @TaskAction
        fun buildBundle() {
            val staging = recreate(File(temporaryDir, "staging"))
            val androidDir = File(staging, "android").also { it.mkdirs() }

            val classesJar = extractClassesJar(staging)
            failOnHostOwnedClasses(classesJar)
            failOnDeniedReferences(classesJar)
            copyPreparedResources(classesJar, androidDir)
            dex(classesJar, androidDir)

            val unsigned = File(staging, "unsigned.zip")
            DeterministicZip.pack(androidDir, unsigned, prefix = "android/")

            val signed = File(staging, "signed.zip")
            unsigned.copyTo(signed, overwrite = true)
            sign(signed)

            val target = recreate(outputDirectory.get().asFile)
            val bundle = File(target, bundleFileName.get())
            // Signing stamps entries with wall-clock times; normalise them so rebuilds of unchanged
            // sources keep the same SHA-256 and the dataStore config does not have to be re-edited.
            DeterministicZip.repack(signed, bundle)

            report(bundle, target)
        }

        private fun extractClassesJar(staging: File): File {
            val aarFile = aar.get().asFile
            val classesJar = File(staging, "classes.jar")
            ZipFile(aarFile).use { zip ->
                val entry =
                    zip.getEntry("classes.jar")
                        ?: throw GradleException("classes.jar not found inside $aarFile")
                zip.getInputStream(entry).use { input ->
                    classesJar.outputStream().use { output -> input.copyTo(output) }
                }
            }
            return classesJar
        }

        private fun failOnHostOwnedClasses(classesJar: File) {
            val forbidden = ClassesJarInspector.forbiddenEntries(classesJar)
            if (forbidden.isEmpty()) return
            throw GradleException(
                "This module packages classes the host already owns, which would be loaded twice and " +
                    "fail with ClassCastException or NoSuchMethodError. Keep plugin-sdk and every " +
                    "compose.* dependency (except compose.components.resources) compileOnly. Offending " +
                    "entries: ${forbidden.take(MAX_REPORTED_ENTRIES).joinToString(", ")}" +
                    if (forbidden.size > MAX_REPORTED_ENTRIES) " (+${forbidden.size - MAX_REPORTED_ENTRIES} more)" else "",
            )
        }

        /**
         * Fails on references the host's plugin class loader will refuse.
         *
         * Without this the author would learn about it as a `ClassNotFoundException` on a device,
         * with nothing to say why the class is missing. It is a static check and so cannot see a
         * name built at runtime — the host enforces the same list when it loads the DEX.
         */
        private fun failOnDeniedReferences(classesJar: File) {
            val denied = ClassesJarInspector.deniedReferences(classesJar)
            if (denied.isEmpty()) return

            val detail =
                denied.entries
                    .take(MAX_REPORTED_ENTRIES)
                    .joinToString("\n") { (owner, references) ->
                        "  $owner references ${references.joinToString(", ")}"
                    }

            throw GradleException(
                "This plugin references classes the DHIS2 Capture App does not expose to plugins, so " +
                    "they will not resolve at runtime:\n$detail\n" +
                    "DHIS2 data access goes through Dhis2PluginContext.sdk, which is the SDK scoped to " +
                    "what the server granted this plugin. The host's own classes, the unrestricted D2 " +
                    "entry points and Koin's global context are deliberately out of reach.",
            )
        }

        private fun copyPreparedResources(
            classesJar: File,
            androidDir: File,
        ) {
            val roots = preparedResources.files.filter { it.isDirectory && it.walkTopDown().any { file -> file.isFile } }
            if (roots.isEmpty()) {
                logger.info("No prepared Compose resources found; the bundle will contain classes only.")
                return
            }

            val packageName =
                resourcePackage.orNull
                    ?: ClassesJarInspector.resourcePackage(classesJar)
                    ?: throw GradleException(
                        "This module has Compose resources but no generated Res class was found in its " +
                            "classes.jar, so the package they are addressed by is unknown. Set " +
                            "pluginBundle.resourcePackage to the value of compose.resources.packageOfResClass.",
                    )

            val resourcesRoot = File(androidDir, "composeResources/$packageName")
            roots.forEach { root ->
                root.walkTopDown().filter { it.isFile }.forEach { source ->
                    val destination = File(resourcesRoot, source.relativeTo(root).path)
                    destination.parentFile?.mkdirs()
                    source.copyTo(destination, overwrite = true)
                }
            }
            logger.info("Staged Compose resources under composeResources/{}", packageName)
        }

        private fun dex(
            classesJar: File,
            androidDir: File,
        ) {
            val dexOutput = File(temporaryDir, "dex").also { recreate(it) }
            execute(
                tool = d8Executable.get().asFile,
                args =
                    listOf(
                        "--min-api",
                        minApi.get().toString(),
                        "--output",
                        dexOutput.absolutePath,
                        classesJar.absolutePath,
                    ),
                description = "d8",
            )

            val produced = File(dexOutput, "classes.dex")
            if (!produced.exists()) {
                throw GradleException("d8 did not produce classes.dex in $dexOutput")
            }
            produced.copyTo(File(androidDir, "classes.dex"), overwrite = true)
        }

        private fun sign(zip: File) {
            val keystore = signing.keystore.get().asFile
            if (!keystore.exists()) {
                throw GradleException(
                    "Signing keystore not found at $keystore. Install Android Studio to get the debug " +
                        "keystore, create one with `keytool -genkey -v -keystore ~/.android/debug.keystore " +
                        "-storepass android -alias androiddebugkey -keypass android -dname " +
                        "'CN=Android Debug,O=Android,C=US' -keyalg RSA -keysize 2048 -validity 10000`, or " +
                        "configure pluginBundle.signing.",
                )
            }

            execute(
                tool = apksignerExecutable.get().asFile,
                args =
                    listOf(
                        "sign",
                        "--ks",
                        keystore.absolutePath,
                        "--ks-key-alias",
                        signing.alias.get(),
                        // Passwords go through the environment rather than argv, which any other
                        // process on the machine can read.
                        "--ks-pass",
                        "env:$STORE_PASSWORD_VARIABLE",
                        "--key-pass",
                        "env:$KEY_PASSWORD_VARIABLE",
                        // v1 is the JAR signature scheme the host verifies; the APK-only schemes
                        // would just add unverifiable blocks to a zip that is not an APK.
                        "--v1-signing-enabled",
                        "true",
                        "--v2-signing-enabled",
                        "false",
                        "--v3-signing-enabled",
                        "false",
                        "--v4-signing-enabled",
                        "false",
                        "--min-sdk-version",
                        minApi.get().toString(),
                        zip.absolutePath,
                    ),
                description = "apksigner",
                environment =
                    mapOf(
                        STORE_PASSWORD_VARIABLE to signing.storePassword.get(),
                        KEY_PASSWORD_VARIABLE to signing.keyPassword.get(),
                    ),
            )
        }

        private fun report(
            bundle: File,
            target: File,
        ) {
            val checksum = "sha256:" + sha256(bundle)
            File(target, "${bundle.name}.sha256").writeText("$checksum\n")
            if (emitDataStoreSnippet.get()) {
                File(target, DATA_STORE_SNIPPET).writeText(dataStoreSnippet(checksum))
            }

            logger.lifecycle("")
            logger.lifecycle("Built plugin bundle")
            logger.lifecycle("  path:     ${bundle.absolutePath}")
            logger.lifecycle("  size:     ${bundle.length()} bytes")
            logger.lifecycle("  checksum: $checksum")
            if (emitDataStoreSnippet.get()) {
                logger.lifecycle("  config:   ${File(target, DATA_STORE_SNIPPET).absolutePath}")
            }
            logger.lifecycle("")
        }

        private fun dataStoreSnippet(checksum: String): String =
            DataStoreSnippet.render(
                pluginId = pluginId.get(),
                version = pluginVersion.get(),
                entryPoint = entryPoint.get(),
                bundleFileName = bundleFileName.get(),
                checksum = checksum,
            )

        private fun execute(
            tool: File,
            args: List<String>,
            description: String,
            environment: Map<String, String> = emptyMap(),
        ) {
            val output = ByteArrayOutputStream()
            val result =
                execOperations.exec { spec ->
                    spec.executable = tool.absolutePath
                    spec.args(args)
                    environment.forEach { (key, value) -> spec.environment(key, value) }
                    spec.standardOutput = output
                    spec.errorOutput = output
                    spec.isIgnoreExitValue = true
                }
            if (result.exitValue != 0) {
                throw GradleException("$description failed with exit ${result.exitValue}:\n$output")
            }
            logger.info("{} output:\n{}", description, output)
        }

        private fun sha256(file: File): String =
            MessageDigest
                .getInstance("SHA-256")
                .digest(file.readBytes())
                .joinToString("") { "%02x".format(it) }

        private fun recreate(directory: File): File =
            directory.apply {
                deleteRecursively()
                mkdirs()
            }

        private companion object {
            const val DATA_STORE_SNIPPET = "plugin-config.json"
            const val MAX_REPORTED_ENTRIES = 5
            const val STORE_PASSWORD_VARIABLE = "DHIS2_PLUGIN_BUNDLE_STORE_PASSWORD"
            const val KEY_PASSWORD_VARIABLE = "DHIS2_PLUGIN_BUNDLE_KEY_PASSWORD"
        }
    }
