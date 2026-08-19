package org.dhis2.mobile.plugin.gradle

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * `pluginBundle { }` — configuration for [BuildPluginBundleTask].
 *
 * Every property has a working default, so a plugin project normally configures nothing: the
 * bundle is named from the Gradle module and its `version`, resources are discovered from the
 * Compose resource generator, and signing uses the Android debug keystore.
 */
abstract class PluginBundleExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        /**
         * Package the generated Compose `Res` class lives in, which is also the path resources are
         * addressed by inside the bundle (`android/composeResources/{package}/…`).
         *
         * Left unset it is read back from the compiled `Res` class, so it cannot drift from
         * `compose.resources.packageOfResClass`. Set it only when that detection fails.
         */
        abstract val resourcePackage: Property<String>

        /** `--min-api` handed to d8. Defaults to 26, the floor for `InMemoryDexClassLoader`. */
        abstract val minApi: Property<Int>

        /** Bundle file name. Defaults to `{module}-{version}.zip`. */
        abstract val bundleFileName: Property<String>

        /** Directory the bundle and its checksum are written to. */
        abstract val outputDirectory: DirectoryProperty

        /** Overrides the `d8` discovered in the Android SDK's build-tools. */
        abstract val d8Executable: RegularFileProperty

        /** Overrides the `apksigner` discovered in the Android SDK's build-tools. */
        abstract val apksignerExecutable: RegularFileProperty

        /**
         * Whether to check this project's Kotlin, Compose, SDK and JVM-target settings against the
         * host they must run inside. Defaults to `true`; turning it off trades build-time errors for
         * runtime failures on device.
         */
        abstract val verifyToolchain: Property<Boolean>

        /**
         * Whether to write a `plugin-config.json` next to the bundle: the dataStore entry the DHIS2
         * administrator needs, with `version` and `checksum` already filled in. Defaults to `true`.
         */
        abstract val emitDataStoreSnippet: Property<Boolean>

        /**
         * `id` written into the emitted `plugin-config.json`. Defaults to an obvious placeholder.
         *
         * This and [entryPoint] are the only fields of that snippet a build cannot work out for
         * itself, and they are here so the file is postable as it is rather than needing the same
         * two edits after every build. They feed the snippet and nothing else: neither value
         * reaches the bundle, and the server dataStore remains the single source of truth for a
         * plugin's identity — the host reads the id and entry point from there, never from anything
         * the plugin shipped.
         */
        abstract val pluginId: Property<String>

        /**
         * Fully qualified name of the class implementing `Dhis2Plugin`, written into the emitted
         * `plugin-config.json`. Defaults to an obvious placeholder. See [pluginId].
         */
        abstract val entryPoint: Property<String>

        /** Signing configuration. See [SigningSpec]. */
        val signing: SigningSpec = objects.newInstance(SigningSpec::class.java)

        /** Configures [signing]. */
        fun signing(action: Action<in SigningSpec>) {
            action.execute(signing)
        }
    }
