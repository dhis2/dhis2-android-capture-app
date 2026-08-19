package org.dhis2.mobile.plugin.sdk

import org.dhis2.mobile.plugin.sdk.dto.DataValueDto
import org.dhis2.mobile.plugin.sdk.dto.TrackedEntityInstanceDto
import org.hisp.dhis.android.core.scopedaccess.ScopedD2

/**
 * A plugin's gateway to DHIS2 data.
 *
 * [sdk] is the DHIS2 Android SDK itself, narrowed to the scope the server granted this plugin. The
 * repositories it hands back are ordinary SDK repositories, so a plugin gets the whole fluent API —
 * filters, ordering, paging, children, `blockingGet()` — at full granularity, with no wrapper in the
 * way and no per-call round trip through the host.
 *
 * The narrowing is not advisory. SDK repository filters are append-only and copy-on-write, so a
 * repository that arrives carrying `byProgramUid().in(granted)` can only ever be narrowed further:
 * asking for a program outside the grant returns nothing rather than something wider. Writes are
 * checked against the grant as they happen, on the object actually being written. Both mechanisms
 * live in the SDK; see `ScopedD2` for the details and for what is deliberately not exposed.
 *
 * ```kotlin
 * @Composable
 * override fun content(context: Dhis2PluginContext) {
 *     val overdue by produceState(emptyList<Event>()) {
 *         value = context.sdk.events()
 *             .byStatus().eq(EventStatus.OVERDUE)
 *             .orderByDueDate(RepositoryScope.OrderByDirection.ASC)
 *             .blockingGet()
 *     }
 *     Text("${overdue.size} overdue")
 * }
 * ```
 *
 * The scope is decided entirely by the server config ([PluginMetadata.scope]); a plugin can read
 * what it was granted from [pluginMetadata] but cannot change it.
 *
 * **This is an API boundary, not a sandbox.** Plugins run in the host process, so this constrains
 * code that goes through it and says nothing about code that goes around it.
 */
interface Dhis2PluginContext {
    /** The server-authored metadata this plugin was loaded with, including its granted scope. */
    val pluginMetadata: PluginMetadata

    /** The DHIS2 SDK, restricted to [PluginMetadata.effectiveScope]. */
    val sdk: ScopedD2

    /**
     * Returns tracked entity instances enrolled in [programUid].
     *
     * Fails with [SecurityException] if [programUid] is outside the granted scope.
     */
    @Deprecated(
        message = "Use sdk.trackedEntityInstances(), which offers the SDK's full query API.",
        replaceWith = ReplaceWith("sdk.trackedEntityInstances().byProgramUids(listOf(programUid)).blockingGet()"),
    )
    suspend fun getTrackedEntityInstances(programUid: String): Result<List<TrackedEntityInstanceDto>>

    /**
     * Returns data values for the given [dataSetUid], [orgUnitUid] and [period].
     *
     * Fails with [SecurityException] if any of them is outside the granted scope.
     */
    @Deprecated(
        message = "Use sdk.dataValues(), which offers the SDK's full query API.",
        replaceWith = ReplaceWith("sdk.dataValues().byOrganisationUnitUid().eq(orgUnitUid).byPeriod().eq(period)"),
    )
    suspend fun getDataValues(
        orgUnitUid: String,
        dataSetUid: String,
        period: String,
    ): Result<List<DataValueDto>>

    /**
     * Saves a single [dataValue].
     *
     * Fails with [SecurityException] if the value falls outside the granted scope — which, unlike
     * the version this replaces, is checked against the data element actually being written rather
     * than the [dataSetUid] argument.
     */
    @Deprecated(
        message = "Use sdk.dataValues().value(...), which is scope-checked on the value itself.",
        replaceWith = ReplaceWith("sdk.dataValues().value(period, orgUnit, dataElement, coc, aoc)"),
    )
    suspend fun saveDataValue(
        dataSetUid: String,
        dataValue: DataValueDto,
    ): Result<Unit>
}
