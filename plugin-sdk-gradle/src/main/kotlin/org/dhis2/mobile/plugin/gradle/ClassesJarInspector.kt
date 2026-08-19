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
     * `plugin-sdk` and Compose belong to the host process, and the Kotlin runtime comes with it.
     */
    private val HOST_OWNED_PREFIXES =
        listOf(
            "org/dhis2/mobile/plugin/sdk/",
            "androidx/compose/",
            "org/jetbrains/compose/",
            "kotlin/",
            // The DHIS2 SDK and Koin both live in the host process. A plugin bundling its own copy
            // of either gets a second set of types that are not the host's, so a ScopedD2 handed to
            // it would not be assignable to the copy it compiled against.
            "org/hisp/dhis/",
            "org/koin/",
        )

    /**
     * Classes a plugin must not *reference*, mirroring
     * `org.dhis2.mobile.plugin.security.PluginClassLoaderPolicy` on the host side.
     *
     * The host refuses to load these at runtime; catching them here turns a
     * `ClassNotFoundException` on someone's device into a build failure on the author's machine,
     * with a message that says why.
     *
     * Stored in JVM internal form (slashes), which is how they appear in a class file's constant
     * pool.
     */
    private val DENIED_REFERENCE_PREFIXES =
        listOf(
            "org/hisp/dhis/android/core/D2",
            "org/hisp/dhis/android/core/arch/d2/",
            "org/hisp/dhis/android/core/arch/db/",
            "org/hisp/dhis/android/core/arch/api/",
            "org/hisp/dhis/android/core/wipe/",
            "org/hisp/dhis/android/core/datastore/",
            "org/hisp/dhis/android/core/user/",
            "org/koin/core/context/",
            "org/koin/mp/KoinPlatformTools",
            "org/dhis2/",
        )

    /** Exceptions to [DENIED_REFERENCE_PREFIXES] — the plugin API itself lives under `org/dhis2/`. */
    private val ALLOWED_REFERENCE_PREFIXES = listOf("org/dhis2/mobile/plugin/sdk/")

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

    /**
     * Class names referenced by [jar] that the host will refuse to load, as `class -> references`.
     *
     * Reads each class file's constant pool, which is where every type a class mentions is recorded
     * — so this sees `Class.forName("…")`-free references such as a direct `D2Manager.getD2()` call.
     *
     * It does **not** see names assembled at runtime: `Class.forName("org.hisp" + ".…")` passes.
     * That is inherent to a static check and is why the host enforces the same list at load time.
     * The value here is a clear build error for the honest mistake, not a barrier against evasion.
     */
    fun deniedReferences(jar: File): Map<String, List<String>> =
        ZipFile(jar).use { zip ->
            val entries =
                zip
                    .entries()
                    .asSequence()
                    .filter { it.name.endsWith(".class") }
                    .toList()

            // A plugin's own classes are exempt whatever they are called. The sample plugin lives in
            // `org.dhis2.pluginimplementationtest`, so the `org/dhis2/` rule — which is about the
            // *host's* classes — would otherwise reject every plugin that picks a DHIS2-ish package
            // name. Only references leaving this jar can be resolved from the host, so only those
            // are the class loader's business.
            val ownClasses = entries.mapTo(HashSet()) { it.name.removeSuffix(".class") }

            entries
                .mapNotNull { entry ->
                    val denied =
                        zip.getInputStream(entry).use { input ->
                            deniedReferencesIn(ConstantPoolReader.classNames(input.readBytes()), ownClasses)
                        }
                    if (denied.isEmpty()) null else entry.name.removeSuffix(".class") to denied
                }.toMap()
        }

    /** Pure counterpart of [deniedReferences], for testing. */
    @JvmOverloads
    fun deniedReferencesIn(
        referencedClasses: Collection<String>,
        ownClasses: Set<String> = emptySet(),
    ): List<String> =
        referencedClasses
            .filter { name ->
                name !in ownClasses &&
                    // Nested and synthetic classes are referenced as `Owner$Inner`, which is not an
                    // entry name on its own for every compiler shape; match the outer class too.
                    name.substringBefore('$') !in ownClasses &&
                    ALLOWED_REFERENCE_PREFIXES.none { name.startsWith(it) } &&
                    DENIED_REFERENCE_PREFIXES.any { name.startsWith(it) }
            }.distinct()
            .sorted()

    private fun entryNames(jar: File): List<String> =
        ZipFile(jar).use { zip ->
            zip
                .entries()
                .asSequence()
                .map { it.name }
                .toList()
        }
}
