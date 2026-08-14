package org.dhis2.mobile.plugin.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

/**
 * Keystore used to sign the plugin bundle.
 *
 * Defaults to the standard Android debug keystore (`~/.android/debug.keystore`, alias
 * `androiddebugkey`, password `android`), which is fine for local iteration. Production publishers
 * point this at their own key:
 *
 * ```kotlin
 * pluginBundle {
 *     signing {
 *         keystore = file("release.keystore")
 *         alias = "publisher"
 *         storePassword = providers.gradleProperty("pluginKeystorePassword")
 *         keyPassword = providers.gradleProperty("pluginKeyPassword")
 *     }
 * }
 * ```
 *
 * The host verifies the signature but does not pin a certificate today, so any valid signature is
 * accepted — the key identifies the publisher for future certificate allow-listing.
 *
 * Passwords are handed to `apksigner` through the environment rather than the command line, so they
 * do not show up in the process list of a shared machine.
 */
abstract class SigningSpec {
    /** Keystore holding the signing key. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val keystore: RegularFileProperty

    /** Alias of the key inside [keystore]. */
    @get:Input
    abstract val alias: Property<String>

    /** Keystore password. Deliberately not a task input: secrets never enter build caches or logs. */
    @get:Internal
    abstract val storePassword: Property<String>

    /** Key password. Deliberately not a task input, for the same reason as [storePassword]. */
    @get:Internal
    abstract val keyPassword: Property<String>
}
