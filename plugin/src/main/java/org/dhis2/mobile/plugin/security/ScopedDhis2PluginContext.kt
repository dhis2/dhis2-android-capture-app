package org.dhis2.mobile.plugin.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.dhis2.mobile.plugin.sdk.dto.DataValueDto
import org.dhis2.mobile.plugin.sdk.dto.TrackedEntityInstanceDto
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.datavalue.LegacyDataValueApi
import timber.log.Timber

/**
 * Host-side implementation of [Dhis2PluginContext].
 *
 * Enforces the security contract declared in [pluginMetadata]:
 * - All read/write operations are restricted to [PluginMetadata.allowedProgramUids] and
 *   [PluginMetadata.allowedDataSetUids].
 * - Returns a [Result.failure] with [SecurityException] (never throws) for out-of-scope access.
 * - Returns DTOs only; raw DHIS2 Android SDK types are never exposed to plugin code.
 *
 * All blocking D2 calls are wrapped in [Dispatchers.IO].
 */
class ScopedDhis2PluginContext(
    override val pluginMetadata: PluginMetadata,
    private val d2: D2,
) : Dhis2PluginContext {

    // ── Read ─────────────────────────────────────────────────────────────────

    override suspend fun getTrackedEntityInstances(
        programUid: String,
    ): Result<List<TrackedEntityInstanceDto>> {
        if (programUid !in pluginMetadata.allowedProgramUids) {
            return scopeViolation("program", programUid)
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                val teis = d2.trackedEntityModule()
                    .trackedEntityInstances()
                    .byProgramUids(listOf(programUid))
                    .blockingGet()

                teis.map { tei ->
                    val attributes = d2.trackedEntityModule()
                        .trackedEntityAttributeValues()
                        .byTrackedEntityInstance()
                        .eq(tei.uid())
                        .blockingGet()
                        .associate { it.trackedEntityAttribute()!! to (it.value() ?: "") }

                    TrackedEntityInstanceDto(
                        uid = tei.uid(),
                        programUid = programUid,
                        attributes = attributes,
                    )
                }
            }
        }
    }

    override suspend fun getDataValues(
        orgUnitUid: String,
        dataSetUid: String,
        period: String,
    ): Result<List<DataValueDto>> {
        if (dataSetUid !in pluginMetadata.allowedDataSetUids) {
            return scopeViolation("dataset", dataSetUid)
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                // Resolve the data elements belonging to this dataset to scope the query.
                val dataElementUids = d2.dataSetModule()
                    .dataSets()
                    .withDataSetElements()
                    .uid(dataSetUid)
                    .blockingGet()
                    ?.dataSetElements()
                    ?.mapNotNull { it.dataElement()?.uid() }
                    ?: emptyList()

                d2.dataValueModule()
                    .dataValues()
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .byPeriod().eq(period)
                    .byDataElementUid().`in`(dataElementUids)
                    .blockingGet()
                    .filter { it.deleted() != true }
                    .map { dv ->
                        DataValueDto(
                            dataElementUid = dv.dataElement()!!,
                            value = dv.value() ?: "",
                            period = dv.period()!!,
                            orgUnitUid = dv.organisationUnit()!!,
                            categoryOptionComboUid = dv.categoryOptionCombo() ?: "",
                        )
                    }
            }
        }
    }

    // ── Write ────────────────────────────────────────────────────────────────

    @OptIn(LegacyDataValueApi::class)
    override suspend fun saveDataValue(
        dataSetUid: String,
        dataValue: DataValueDto,
    ): Result<Unit> {
        if (dataSetUid !in pluginMetadata.allowedDataSetUids) {
            return scopeViolation("dataset", dataSetUid)
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                val repo = d2.dataValueModule()
                    .dataValues()
                    .value(
                        dataValue.period,
                        dataValue.orgUnitUid,
                        dataValue.dataElementUid,
                        dataValue.categoryOptionComboUid.ifEmpty { "HllvX50cXC0" },
                        "",
                    )

                if (dataValue.value.isEmpty()) {
                    repo.blockingDeleteIfExist()
                } else {
                    repo.blockingSet(dataValue.value)
                }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun <T> scopeViolation(type: String, uid: String): Result<T> {
        val message =
            "Plugin '${pluginMetadata.id}' attempted to access $type '$uid' " +
                "which is not declared in its allowedProgramUids/allowedDataSetUids"
        Timber.e(message)
        return Result.failure(SecurityException(message))
    }
}

/** Factory that creates a [ScopedDhis2PluginContext] bound to a specific plugin's metadata. */
class ScopedDhis2PluginContextFactory(private val d2: D2) {
    fun create(metadata: PluginMetadata): ScopedDhis2PluginContext =
        ScopedDhis2PluginContext(metadata, d2)
}
