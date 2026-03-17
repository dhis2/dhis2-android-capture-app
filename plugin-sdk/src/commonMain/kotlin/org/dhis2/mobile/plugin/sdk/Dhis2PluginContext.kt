package org.dhis2.mobile.plugin.sdk

import org.dhis2.mobile.plugin.sdk.dto.DataValueDto
import org.dhis2.mobile.plugin.sdk.dto.TrackedEntityInstanceDto

/**
 * Security facade that is the **only** gateway through which a plugin may access DHIS2 data.
 *
 * All operations are automatically scoped to the programs and data sets declared in
 * [pluginMetadata]. Attempts to access out-of-scope resources return a failure [Result]
 * containing a [SecurityException]; they never silently return empty data.
 *
 * Plugin developers receive a [Dhis2PluginContext] as a parameter to [Dhis2Plugin.content].
 * The host app provides a concrete implementation ([ScopedDhis2PluginContext]) at runtime.
 */
interface Dhis2PluginContext {
    /** The metadata of the plugin this context was created for. */
    val pluginMetadata: PluginMetadata

    // ── Read operations ──────────────────────────────────────────────────────

    /**
     * Returns all tracked entity instances enrolled in [programUid] that are available
     * offline on this device.
     *
     * Fails with [SecurityException] if [programUid] is not in [PluginMetadata.allowedProgramUids].
     */
    suspend fun getTrackedEntityInstances(programUid: String): Result<List<TrackedEntityInstanceDto>>

    /**
     * Returns data values for the given [dataSetUid], [orgUnitUid], and [period].
     *
     * Fails with [SecurityException] if [dataSetUid] is not in [PluginMetadata.allowedDataSetUids].
     */
    suspend fun getDataValues(
        orgUnitUid: String,
        dataSetUid: String,
        period: String,
    ): Result<List<DataValueDto>>

    // ── Write operations ─────────────────────────────────────────────────────

    /**
     * Saves a single [dataValue] for the given [dataSetUid].
     *
     * The value is persisted locally and will be synced to the server on the next sync cycle.
     * Fails with [SecurityException] if [dataSetUid] is not in [PluginMetadata.allowedDataSetUids].
     */
    suspend fun saveDataValue(
        dataSetUid: String,
        dataValue: DataValueDto,
    ): Result<Unit>
}
