package org.dhis2.mobile.plugin.sdk

import org.hisp.dhis.android.core.D2

/**
 * The gateway through which a plugin reaches DHIS2 data.
 *
 * [sdk] is **the DHIS2 Android SDK itself**. A plugin gets the whole fluent API — filters, ordering,
 * paging, children, `blockingGet()` — with no wrapper in the way:
 *
 * ```kotlin
 * val overdue = context.sdk.eventModule().events()
 *     .byStatus().eq(EventStatus.OVERDUE)
 *     .byOrganisationUnitUid().eq(clinicUid)
 *     .blockingGet()
 * ```
 *
 * **This grants unrestricted access.** There is no filtering here: a plugin holding this context can
 * read and write anything the logged-in user can, including wiping the database. The only control is
 * which plugins an administrator chooses to run — see §6 of `docs/plugin-system.md`. Narrowing that
 * access to a declared subset is the next iteration of this work, and it belongs in the SDK rather
 * than here, so that a future out-of-process host can sit in front of the same restriction.
 *
 * Lives in `androidMain` because [D2] is the DHIS2 *Android* SDK and has no common-source
 * equivalent. [PluginMetadata] and [InjectionPoint] stay in `commonMain`.
 *
 * `blockingGet()` and friends must not run on the main thread; wrap them in `Dispatchers.IO`.
 */
interface Dhis2PluginContext {
    /**
     * The plugin's server-authored configuration.
     *
     * Read from the DHIS2 dataStore, never declared by the plugin, so there is exactly one place to
     * change a plugin's identity.
     */
    val pluginMetadata: PluginMetadata

    /** The DHIS2 Android SDK, unrestricted. */
    val sdk: D2
}
