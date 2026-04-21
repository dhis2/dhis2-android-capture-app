package org.dhis2.mobile.plugin.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import org.dhis2.mobile.plugin.sdk.InjectionPoint

/**
 * Returns all plugins registered for the given [InjectionPoint], paired with their
 * resource roots.
 */
class GetPluginSlotContent(
    private val pluginRegistry: PluginRegistry,
) : UseCase<InjectionPoint, List<RegisteredPlugin>> {
    override suspend fun invoke(input: InjectionPoint): Result<List<RegisteredPlugin>> =
        Result.success(pluginRegistry.getPluginsForSlot(input))
}
