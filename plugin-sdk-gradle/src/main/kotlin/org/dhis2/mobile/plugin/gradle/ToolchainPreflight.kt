package org.dhis2.mobile.plugin.gradle

/**
 * What a plugin project declares, as far as the host cares.
 *
 * A `null` means "could not be determined" — those checks are skipped rather than guessed at, so a
 * future toolchain that hides one of these values degrades to fewer checks instead of false alarms.
 */
internal data class ToolchainState(
    val kotlinVersion: String? = null,
    val composeVersion: String? = null,
    val compileSdk: Int? = null,
    val minSdk: Int? = null,
    val jvmTarget: String? = null,
    val usesLegacyAndroidLibraryPlugin: Boolean = false,
)

/**
 * The compatibility table from §5.1 of the plugin system documentation, as checks.
 *
 * A plugin's DEX is loaded into the running Capture App and resolves Kotlin, Compose and
 * `plugin-sdk` from the *host's* class loader, so mismatches here surface on device as
 * `Module was compiled with an incompatible version of Kotlin`, `NoSuchMethodError` on mangled
 * Compose signatures, or a bundle that will not load at all. Catching them while the plugin author
 * still has a build log is the entire point of running these at configuration time.
 */
internal object ToolchainPreflight {
    /** Human-readable description of everything wrong with [state]; empty when it is compatible. */
    fun problems(state: ToolchainState): List<String> =
        buildList {
            if (state.usesLegacyAndroidLibraryPlugin) {
                add(
                    "The 'com.android.library' plugin is applied. A plugin must use " +
                        "'com.android.kotlin.multiplatform.library' instead — the bundle is built from " +
                        "the Kotlin Multiplatform Android target's AAR.",
                )
            }
            state.kotlinVersion?.let { kotlin ->
                if (kotlin != HostToolchain.KOTLIN) {
                    add(
                        "Kotlin $kotlin does not match the host's ${HostToolchain.KOTLIN}. plugin-sdk is " +
                            "published with the host's Kotlin metadata version, so another compiler cannot " +
                            "read it: expect 'Module was compiled with an incompatible version of Kotlin', " +
                            "usually followed by a cascade of unresolved references in the generated " +
                            "resource accessors.",
                    )
                }
            }
            state.composeVersion?.let { compose ->
                if (compose != HostToolchain.COMPOSE) {
                    add(
                        "Compose Multiplatform $compose does not match the host's ${HostToolchain.COMPOSE}. " +
                            "Compose mangles Composable signatures per version, so the plugin would fail at " +
                            "composition time with NoSuchMethodError on calls like Text or Card.",
                    )
                }
            }
            state.compileSdk?.let { compileSdk ->
                if (compileSdk < HostToolchain.COMPILE_SDK) {
                    add(
                        "compileSdk $compileSdk is below the host's ${HostToolchain.COMPILE_SDK}. " +
                            "plugin-sdk is compiled against the host's compileSdk, so checkAarMetadata " +
                            "fails on plugin-sdk-android.",
                    )
                }
            }
            state.minSdk?.let { minSdk ->
                if (minSdk < HostToolchain.MIN_SDK_FLOOR) {
                    add(
                        "minSdk $minSdk is below ${HostToolchain.MIN_SDK_FLOOR}. Plugins are loaded with " +
                            "InMemoryDexClassLoader, which needs API ${HostToolchain.MIN_SDK_FLOOR}; the host " +
                            "supports older devices but skips the plugin system on them entirely.",
                    )
                }
            }
            majorJvmVersion(state.jvmTarget)?.let { jvmTarget ->
                if (jvmTarget > HostToolchain.JVM_TARGET) {
                    add(
                        "JVM target $jvmTarget is above the host's ${HostToolchain.JVM_TARGET}. The DEX is " +
                            "loaded by the host's runtime, which rejects newer class files with " +
                            "'Unsupported class file major version'. A lower target is safe.",
                    )
                }
            }
        }

    /**
     * Warning for dependencies that will not be in the bundle. An AAR's `classes.jar` holds only the
     * module's own classes, so anything declared `implementation`/`api` is compiled against but not
     * packaged, and has to already exist in the host process.
     */
    fun runtimeDependencyWarning(coordinates: List<String>): String? {
        if (coordinates.isEmpty()) return null
        return "The following dependencies are not compileOnly, but a plugin bundle only ever " +
            "contains this module's own classes — they must already be present in the host app at " +
            "runtime or the plugin will fail with NoClassDefFoundError: " +
            coordinates.sorted().joinToString(", ") +
            ". Declare them compileOnly if the host provides them."
    }

    /** Normalises `"17"`, `"1.8"` and `"JVM_17"` to a major version number. */
    fun majorJvmVersion(jvmTarget: String?): Int? {
        val digits = jvmTarget?.substringAfterLast('_')?.removePrefix("1.") ?: return null
        return digits.toIntOrNull()
    }
}
