package org.dhis2.mobile.plugin.security

/**
 * Decides which host classes a plugin's class loader may resolve.
 *
 * A plugin's DEX is loaded with the host APK's class loader as its parent, so by default it can name
 * every class in the app — including `D2Manager.getD2()`, a public static that returns the
 * unrestricted SDK and makes the whole scoping model decorative. This policy is what stops the
 * obvious routes.
 *
 * **It is a guardrail, not a sandbox.** Plugins run in the host process, and several routes remain
 * open by construction: `Thread.currentThread().contextClassLoader`,
 * `ClassLoader.getSystemClassLoader()`, and the loader of any host object a plugin legitimately
 * holds (`ScopedD2::class.java.classLoader`). Kotlin's `internal` is public in JVM bytecode, so
 * reflection reaches internal constructors too. What this buys is that reaching past the grant has
 * to be deliberate and obvious in review, rather than a one-liner.
 *
 * The rules are intentionally listed rather than computed, so that adding an escape hatch to the SDK
 * does not silently open one here.
 */
internal object PluginClassLoaderPolicy {
    /**
     * Classes a plugin may never resolve, by prefix.
     *
     * Each entry is here for a specific reason, recorded so the list can be maintained rather than
     * cargo-culted.
     */
    private val DENIED_PREFIXES =
        listOf(
            // The unrestricted SDK entry points. D2Manager.getD2() alone defeats every scope.
            "org.hisp.dhis.android.core.D2",
            // The SDK's plumbing: DI component, database adapter, HTTP client, call executors. Its own
            // Koin context lives under arch.d2 and is a service locator for every internal store.
            "org.hisp.dhis.android.core.arch.",
            // Destructive, and out of any scope's reach by design.
            "org.hisp.dhis.android.core.wipe.",
            // Privilege escalation: host configuration, including the plugin config itself, lives here.
            "org.hisp.dhis.android.core.datastore.",
            // Credentials and account management.
            "org.hisp.dhis.android.core.user.",
            // Koin's global service locator — a direct handle on the host's container.
            "org.koin.core.context.",
            "org.koin.mp.KoinPlatformTools",
            // The host application itself: repositories, view models, D2 provider, everything.
            "org.dhis2.",
        )

    /**
     * Exceptions to [DENIED_PREFIXES], for classes a plugin genuinely needs.
     *
     * Two carve-outs, both load-bearing:
     *
     * - The plugin API lives under `org.dhis2.` and the plugin must see *the host's* copy of those
     *   types; a second copy is what produces `ClassCastException: … not assignable to Dhis2Plugin`.
     * - `arch.repositories` is denied by the blanket `arch.` rule above, but it is where the fluent
     *   API actually lives. `byProgramUid()` returns a `StringFilterConnector` from
     *   `…filters.internal`, ordering takes a `RepositoryScope.OrderByDirection`, and tracker search
     *   takes a `RepositoryMode` from `…scope.internal`. Being in a package named `internal` did not
     *   keep these out of the SDK's public signatures, so a plugin cannot use the SDK without them.
     */
    private val ALLOWED_PREFIXES =
        listOf(
            "org.dhis2.mobile.plugin.sdk.",
            "org.hisp.dhis.android.core.arch.repositories.",
            "org.hisp.dhis.android.core.arch.helpers.",
        )

    /**
     * SDK-private packages: stores, call factories, handlers, everything under a `.internal.`
     * segment.
     *
     * Checked *after* [ALLOWED_PREFIXES], because the fluent API's own types sit in `internal`
     * packages and must survive this rule. Only applied to `org.hisp.dhis.` — a plugin's own
     * `…internal…` packages come from its own DEX and never reach this decision.
     */
    private const val INTERNAL_SEGMENT = ".internal."

    /** True if [className] may be resolved through the plugin's class loader. */
    fun isAllowed(className: String): Boolean =
        when {
            ALLOWED_PREFIXES.any { className.startsWith(it) } -> true
            DENIED_PREFIXES.any { className.startsWith(it) } -> false
            className.startsWith("org.hisp.dhis.") && className.contains(INTERNAL_SEGMENT) -> false
            else -> true
        }
}
