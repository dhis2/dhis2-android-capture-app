package org.dhis2.mobile.plugin.security

import org.dhis2.mobile.plugin.sdk.OrgUnitGrant
import org.dhis2.mobile.plugin.sdk.PluginScope
import org.dhis2.mobile.plugin.sdk.UidGrant
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.scopedaccess.D2Capability
import org.hisp.dhis.android.core.scopedaccess.D2DataScope
import org.hisp.dhis.android.core.scopedaccess.OrgUnitScope
import org.hisp.dhis.android.core.scopedaccess.UidScope
import org.hisp.dhis.android.core.scopedaccess.WritableScope
import timber.log.Timber

/**
 * Translates the server-authored [PluginScope] into the [D2DataScope] the SDK enforces.
 *
 * The two models are deliberately separate. [PluginScope] is a wire format: flat, hand-written by an
 * administrator, and tolerant of values this version of the app does not recognise. [D2DataScope] is
 * a closed algebraic model the SDK can reason about. Mapping between them is the one place a
 * malformed config gets turned into something safe.
 *
 * Anything unrecognised is dropped with a warning rather than failing the plugin, and dropping can
 * only ever *narrow* the grant — an unknown capability name is a capability the plugin does not get.
 */
internal fun PluginScope.toD2DataScope(pluginId: String): D2DataScope =
    D2DataScope(
        programs = programs.toUidScope(),
        dataSets = dataSets.toUidScope(),
        trackedEntityTypes = trackedEntityTypes.toUidScope(),
        dataElements = dataElements.toUidScope(),
        orgUnits = orgUnits.toOrgUnitScope(pluginId),
        writable =
            WritableScope(
                programs = writable.programs.toUidScope(),
                dataSets = writable.dataSets.toUidScope(),
                orgUnits = writable.orgUnits.toOrgUnitScope(pluginId),
            ),
        capabilities = capabilities.toCapabilities(pluginId),
    )

private fun UidGrant.toUidScope(): UidScope =
    when {
        all -> UidScope.All
        uids.isEmpty() -> UidScope.None
        else -> UidScope.Only(uids.toSet())
    }

private fun OrgUnitGrant.toOrgUnitScope(pluginId: String): OrgUnitScope =
    when {
        all -> OrgUnitScope.All
        capture -> OrgUnitScope.Capture
        uids.isEmpty() -> OrgUnitScope.None
        else -> OrgUnitScope.Only(uids.toSet(), parseMode(pluginId))
    }

/**
 * Only the hierarchy modes make sense as a grant. `ACCESSIBLE` and `ALL` are resolved against the
 * logged-in user rather than the named roots, so honouring them here would let a config widen itself
 * past the units it lists; they fall back to `DESCENDANTS` like any other unrecognised value.
 */
private fun OrgUnitGrant.parseMode(pluginId: String): OrganisationUnitMode {
    val parsed = runCatching { OrganisationUnitMode.valueOf(mode.uppercase()) }.getOrNull()

    return when (parsed) {
        OrganisationUnitMode.SELECTED,
        OrganisationUnitMode.CHILDREN,
        OrganisationUnitMode.DESCENDANTS,
        -> parsed

        else -> {
            Timber.w(
                "Plugin '%s': org unit mode '%s' is not one of SELECTED, CHILDREN or DESCENDANTS. " +
                    "Falling back to DESCENDANTS.",
                pluginId,
                mode,
            )
            OrganisationUnitMode.DESCENDANTS
        }
    }
}

private fun List<String>.toCapabilities(pluginId: String): Set<D2Capability> =
    mapNotNullTo(mutableSetOf()) { name ->
        runCatching { D2Capability.valueOf(name.uppercase()) }.getOrElse {
            Timber.w(
                "Plugin '%s': unknown capability '%s' in its scope config; ignoring it. " +
                    "The plugin will not be granted that access.",
                pluginId,
                name,
            )
            null
        }
    }
