package org.dhis2.mobile.plugin.gradle

import java.io.File
import java.util.zip.ZipFile

/**
 * Reads the `classes.jar` extracted from the plugin's AAR, before it is dexed.
 *
 * Two questions are answered here, both of which the bundle format depends on:
 *
 * - Does the jar carry classes the host already provides? Those must resolve from the host's class
 *   loader at runtime; a second copy inside the plugin DEX produces
 *   `ClassCastException: … not assignable to Dhis2Plugin` or `NoSuchMethodError` on Compose
 *   signatures. See [forbiddenEntries].
 * - Which package does the generated Compose `Res` class live in? That is the path resources must
 *   be addressed by inside the bundle. See [resourcePackage].
 */
internal object ClassesJarInspector {
    /**
     * Class prefixes the host owns. A plugin compiles against them but must never ship them:
     * `plugin-sdk`, Compose, Koin, the DHIS2 SDK and design system all belong to the host process,
     * and the Kotlin runtime comes with it.
     *
     * This is [PluginConventions.HOST_PROVIDED] expressed as class paths rather than Maven
     * coordinates. The two must stay in step: that list stops a host-provided dependency being
     * declared `implementation`, and this one catches the classes if one slips through anyway.
     * Anything present there and absent here passes the build and then `ClassCastException`s on a
     * device, which is the failure both checks exist to prevent.
     */
    private val HOST_OWNED_PREFIXES =
        listOf(
            "org/dhis2/mobile/plugin/sdk/",
            "androidx/compose/",
            "androidx/lifecycle/",
            "org/jetbrains/compose/",
            "org/hisp/dhis/",
            "org/koin/",
            "kotlin/",
            "kotlinx/coroutines/",
        )

    private val RES_CLASS = Regex("""^(.+)/Res\.class$""")

    /** Entries in [jar] that duplicate host-owned classes. Empty means the jar is clean. */
    fun forbiddenEntries(jar: File): List<String> = forbiddenEntries(entryNames(jar))

    /** Package of the generated Compose `Res` class in [jar], or `null` when it has none. */
    fun resourcePackage(jar: File): String? = resourcePackage(entryNames(jar))

    /** Pure counterpart of [forbiddenEntries], for testing. */
    fun forbiddenEntries(entryNames: List<String>): List<String> =
        entryNames.filter { name ->
            HOST_OWNED_PREFIXES.any { name.startsWith(it) }
        }

    /** Pure counterpart of [resourcePackage], for testing. */
    fun resourcePackage(entryNames: List<String>): String? =
        entryNames
            .asSequence()
            .mapNotNull { RES_CLASS.matchEntire(it)?.groupValues?.get(1) }
            .minByOrNull { it.length }
            ?.replace('/', '.')

    private fun entryNames(jar: File): List<String> =
        ZipFile(jar).use { zip ->
            zip
                .entries()
                .asSequence()
                .map { it.name }
                .toList()
        }
}
