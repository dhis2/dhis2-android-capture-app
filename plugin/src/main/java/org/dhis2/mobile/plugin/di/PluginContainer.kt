package org.dhis2.mobile.plugin.di

import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.hisp.dhis.android.core.scopedaccess.ScopedD2
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * Builds the private Koin container a single plugin is rendered inside.
 *
 * **Why private.** Loading a plugin's module into the *application* container let it resolve every
 * host binding — `D2` included — and, because Koin allows override by default, silently *replace*
 * them for the rest of the app. So each plugin gets its own container, and this is the only place
 * that decides what goes into it.
 *
 * **Why seeded at all.** The container used to hold nothing, which is safe but unusable: a plugin
 * repository had no way to obtain the object it exists to wrap, so every author had to thread
 * `context.sdk` through each call site by hand. It now holds exactly what the plugin is already
 * given through [Dhis2PluginContext] — its [ScopedD2], its [PluginMetadata], and the context
 * itself. That is a convenience, not a widening: the same objects, reachable by injection rather
 * than by parameter.
 *
 * Nothing else is seeded, and nothing host-owned is reachable from here. What a plugin may read and
 * write is decided by the grant carried on that [ScopedD2], not by what is in this container.
 */
internal object PluginContainer {
    /**
     * @param context the scoped context built from the server-authored metadata.
     * @param pluginModule the plugin's own bindings, or null if it declared none — a container is
     *   still created either way, so `koinInject` works without forcing authors to write an empty
     *   module.
     */
    fun create(
        context: Dhis2PluginContext,
        pluginModule: Module?,
    ): KoinApplication =
        koinApplication {
            modules(
                listOfNotNull(
                    module {
                        single { context }
                        single { context.pluginMetadata }
                        // Resolved eagerly on first request rather than held as a field: ScopedD2 is
                        // created lazily by the context, and a plugin that never reads data should
                        // not pay for it.
                        single { context.sdk }
                    },
                    pluginModule,
                ),
            )
        }
}
