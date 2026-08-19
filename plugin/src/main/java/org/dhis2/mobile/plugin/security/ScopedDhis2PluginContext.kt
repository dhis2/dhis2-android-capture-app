package org.dhis2.mobile.plugin.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.dhis2.mobile.plugin.sdk.dto.DataValueDto
import org.dhis2.mobile.plugin.sdk.dto.TrackedEntityInstanceDto
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.scopedaccess.ScopedD2
import timber.log.Timber

/**
 * Host-side implementation of [Dhis2PluginContext].
 *
 * Almost nothing happens here any more, which is the point: enforcement moved into the DHIS2 SDK,
 * where a repository's scope is append-only and travels with every fluent call. This class just
 * translates the server-authored grant and hands over a [ScopedD2].
 *
 * The deprecated DTO methods are kept so plugins written against the previous API keep working; they
 * now run through the same scoped repositories as everything else. Note that
 * [saveDataValue] behaves differently from the version it replaces — see below.
 */
class ScopedDhis2PluginContext(
    override val pluginMetadata: PluginMetadata,
    private val d2: D2,
) : Dhis2PluginContext {
    /**
     * The grant, resolved from metadata alone.
     *
     * Kept separate from [sdk] so the deprecated methods can refuse an out-of-scope request without
     * touching D2 at all — a check that runs after reaching the database has already leaked what it
     * was meant to protect.
     */
    private val dataScope = pluginMetadata.effectiveScope.toD2DataScope(pluginMetadata.id)

    override val sdk: ScopedD2 by lazy { d2.scopedTo(dataScope) }

    @Deprecated("Use sdk.trackedEntityInstances()")
    override suspend fun getTrackedEntityInstances(programUid: String): Result<List<TrackedEntityInstanceDto>> {
        if (!dataScope.programs.allows(programUid)) {
            return scopeViolation("program", programUid)
        }

        return scoped {
            val instances =
                sdk
                    .trackedEntityInstances()
                    .byProgramUids(listOf(programUid))
                    .blockingGet()

            instances.map { tei ->
                val attributes =
                    sdk
                        .trackedEntityAttributeValues()
                        .byTrackedEntityInstance()
                        .eq(tei.uid())
                        .blockingGet()
                        .associate { it.trackedEntityAttribute() to (it.value() ?: "") }

                TrackedEntityInstanceDto(
                    uid = tei.uid(),
                    programUid = programUid,
                    attributes = attributes,
                )
            }
        }
    }

    @Deprecated("Use sdk.dataValues()")
    override suspend fun getDataValues(
        orgUnitUid: String,
        dataSetUid: String,
        period: String,
    ): Result<List<DataValueDto>> {
        if (!dataScope.dataSets.allows(dataSetUid)) {
            return scopeViolation("dataset", dataSetUid)
        }

        return scoped {
            sdk
                .dataValues()
                .byOrganisationUnitUid()
                .eq(orgUnitUid)
                .byPeriod()
                .eq(period)
                .blockingGet()
                .filter { it.deleted() != true }
                .map { value ->
                    DataValueDto(
                        dataElementUid = value.dataElement(),
                        value = value.value() ?: "",
                        period = value.period(),
                        orgUnitUid = value.organisationUnit(),
                        categoryOptionComboUid = value.categoryOptionCombo(),
                    )
                }
        }
    }

    /**
     * Note this is stricter than the implementation it replaces, which checked the [dataSetUid]
     * argument and then wrote whichever data element the caller passed — so a plugin granted one
     * data set could write any data element in the database. The write is now checked against the
     * value actually being written, inside the SDK.
     */
    @Deprecated("Use sdk.dataValues().value(...)")
    override suspend fun saveDataValue(
        dataSetUid: String,
        dataValue: DataValueDto,
    ): Result<Unit> {
        if (!dataScope.dataSets.allows(dataSetUid)) {
            return scopeViolation("dataset", dataSetUid)
        }

        return scoped {
            val repository =
                sdk.dataValues().value(
                    period = dataValue.period,
                    organisationUnit = dataValue.orgUnitUid,
                    dataElement = dataValue.dataElementUid,
                    categoryOptionCombo = dataValue.categoryOptionComboUid.ifEmpty { DEFAULT_COMBO },
                    attributeOptionCombo = DEFAULT_COMBO,
                    sourceDataSet = dataSetUid,
                )

            if (dataValue.value.isEmpty()) {
                repository.blockingDeleteIfExist()
            } else {
                repository.blockingSet(dataValue.value)
            }
        }
    }

    /**
     * Runs [block] off the main thread and restates the SDK's scope refusal as the [SecurityException]
     * this API has always documented, so existing plugins keep seeing the failure shape they expect.
     */
    private suspend fun <T> scoped(block: () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            runCatching { block() }.recoverCatching { error ->
                if (error is D2Error && error.errorCode() == D2ErrorCode.SCOPE_VIOLATION) {
                    Timber.e("Plugin '%s': %s", pluginMetadata.id, error.errorDescription())
                    throw SecurityException(error.errorDescription())
                }
                throw error
            }
        }

    private fun <T> scopeViolation(
        type: String,
        uid: String,
    ): Result<T> {
        val message =
            "Plugin '${pluginMetadata.id}' attempted to access $type '$uid', " +
                "which its server-granted scope does not permit"
        Timber.e(message)
        return Result.failure(SecurityException(message))
    }

    private companion object {
        /** The DHIS2 default category option combo, used when a value names none. */
        const val DEFAULT_COMBO = "HllvX50cXC0"
    }
}

/** Creates a [ScopedDhis2PluginContext] bound to one plugin's server-granted metadata. */
class ScopedDhis2PluginContextFactory(
    private val d2: D2,
) {
    fun create(metadata: PluginMetadata): ScopedDhis2PluginContext = ScopedDhis2PluginContext(metadata, d2)
}
