package org.dhis2.mobile.plugin.di

import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.hisp.dhis.android.core.D2
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * Builds the private Koin container a single plugin is rendered inside.
 *
 * **Why private, even though the plugin already gets `D2`.** This is not about hiding the SDK — the
 * plugin is handed it deliberately. It is about host integrity. Plugin modules used to be loaded
 * into the *application* container with `koin.loadModules(...)`, and Koin allows override by
 * default, so a plugin declaring a binding for a type the host also binds would silently *replace*
 * it for the rest of the app. One plugin could break unrelated screens, and nothing would say so.
 *
 * The container is seeded with what the plugin already receives through [Dhis2PluginContext] — its
 * [D2], its [PluginMetadata], and the context itself — so a plugin's repository can be constructed
 * by Koin instead of having `context.sdk` threaded through every call site.
 */
internal object PluginContainer {
    /**
     * @param context the context built from the server-authored metadata.
     * @param pluginModule the plugin's own bindings, or null if it declared none — a container is
     *   created either way, so `koinInject` works without forcing authors to write an empty module.
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
                        single { context.sdk }
                    },
                    pluginModule,
                ),
            )
        }
}
