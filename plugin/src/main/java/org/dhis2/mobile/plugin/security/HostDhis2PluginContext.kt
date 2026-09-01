package org.dhis2.mobile.plugin.security

import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.hisp.dhis.android.core.D2

/**
 * Host-side implementation of [Dhis2PluginContext].
 *
 * There is nothing to it, and that is the point of this iteration: the plugin gets `D2`, and the
 * host does not stand between them. It replaces a version that checked every call against
 * `allowedProgramUids` / `allowedDataSetUids` — a check that only ever covered three DTO methods and
 * became meaningless the moment the SDK itself was exposed.
 *
 * Named `Host…` rather than `Scoped…` deliberately: nothing here is scoped, and a name promising
 * otherwise would be the most misleading thing in the module. Restricting access is the next
 * iteration and belongs in the SDK, where a repository's scope travels with every fluent call and a
 * future out-of-process host can sit in front of the same enforcement.
 */
class HostDhis2PluginContext(
    override val pluginMetadata: PluginMetadata,
    override val sdk: D2,
) : Dhis2PluginContext

/**
 * Builds a context per plugin at load time.
 *
 * Built once, from the server-authored metadata, and carried on the registry entry — deliberately
 * *not* something a Composable asks a factory for at render time. A factory that mints a context
 * from caller-supplied metadata is a factory a plugin can call itself.
 */
class HostDhis2PluginContextFactory(
    private val d2: D2,
) {
    fun create(metadata: PluginMetadata): Dhis2PluginContext = HostDhis2PluginContext(metadata, d2)
}
