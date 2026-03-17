package org.dhis2.mobile.plugin.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.InjectionPoint

/**
 * Returns all loaded [Dhis2Plugin] instances registered for the given [InjectionPoint].
 */
class GetPluginSlotContent(
    private val pluginRegistry: PluginRegistry,
) : UseCase<InjectionPoint, List<Dhis2Plugin>> {
    override suspend fun invoke(input: InjectionPoint): Result<List<Dhis2Plugin>> =
        Result.success(pluginRegistry.getPluginsForSlot(input))
}
