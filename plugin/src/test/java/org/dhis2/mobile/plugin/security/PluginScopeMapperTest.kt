package org.dhis2.mobile.plugin.security

import org.dhis2.mobile.plugin.sdk.OrgUnitGrant
import org.dhis2.mobile.plugin.sdk.PluginCapability
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.dhis2.mobile.plugin.sdk.PluginScope
import org.dhis2.mobile.plugin.sdk.UidGrant
import org.dhis2.mobile.plugin.sdk.WritableGrant
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.scopedaccess.D2Capability
import org.hisp.dhis.android.core.scopedaccess.OrgUnitScope
import org.hisp.dhis.android.core.scopedaccess.UidScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The wire format an administrator writes by hand, turned into the closed model the SDK enforces.
 *
 * The property worth pinning throughout: anything malformed or unrecognised has to *narrow* the
 * grant, never widen it.
 */
class PluginScopeMapperTest {
    private fun scope(block: PluginScope.() -> PluginScope = { this }) = PluginScope().block()

    private fun map(scope: PluginScope) = scope.toD2DataScope("org.test.plugin")

    @Test
    fun `an empty scope grants nothing`() {
        val mapped = map(scope())

        assertEquals(UidScope.None, mapped.programs)
        assertEquals(UidScope.None, mapped.dataSets)
        assertEquals(OrgUnitScope.None, mapped.orgUnits)
        assertTrue(mapped.capabilities.isEmpty())
    }

    @Test
    fun `named uids become an Only scope`() {
        val mapped = map(scope { copy(programs = UidGrant(uids = listOf("p1", "p2"))) })

        assertEquals(UidScope.Only(setOf("p1", "p2")), mapped.programs)
    }

    @Test
    fun `an explicit all wins over an empty uid list`() {
        assertEquals(UidScope.All, map(scope { copy(programs = UidGrant(all = true)) }).programs)
    }

    @Test
    fun `org unit mode is carried through`() {
        val mapped =
            map(
                scope { copy(orgUnits = OrgUnitGrant(uids = listOf("ou1"), mode = "SELECTED")) },
            )

        assertEquals(OrgUnitScope.Only(setOf("ou1"), OrganisationUnitMode.SELECTED), mapped.orgUnits)
    }

    @Test
    fun `an unknown org unit mode falls back to descendants rather than failing`() {
        val mapped = map(scope { copy(orgUnits = OrgUnitGrant(uids = listOf("ou1"), mode = "nonsense")) })

        assertEquals(OrgUnitScope.Only(setOf("ou1"), OrganisationUnitMode.DESCENDANTS), mapped.orgUnits)
    }

    @Test
    fun `user-relative org unit modes are not honoured as a grant`() {
        // ACCESSIBLE and ALL resolve against the logged-in user, not the roots the config named, so
        // honouring them would let a config reach past the units it listed.
        for (mode in listOf("ACCESSIBLE", "ALL")) {
            val mapped = map(scope { copy(orgUnits = OrgUnitGrant(uids = listOf("ou1"), mode = mode)) })

            assertEquals(
                OrgUnitScope.Only(setOf("ou1"), OrganisationUnitMode.DESCENDANTS),
                mapped.orgUnits,
            )
        }
    }

    @Test
    fun `capabilities are parsed by name`() {
        val mapped =
            map(
                scope { copy(capabilities = listOf("READ_METADATA", "read_event")) },
            )

        assertEquals(setOf(D2Capability.READ_METADATA, D2Capability.READ_EVENT), mapped.capabilities)
    }

    @Test
    fun `an unknown capability is dropped rather than taking the plugin down`() {
        val mapped =
            map(
                scope { copy(capabilities = listOf("READ_EVENT", "READ_MINDS")) },
            )

        assertEquals(setOf(D2Capability.READ_EVENT), mapped.capabilities)
    }

    @Test
    fun `writable is mapped separately from the read grant`() {
        val mapped =
            map(
                scope {
                    copy(
                        dataSets = UidGrant(uids = listOf("ds1", "ds2")),
                        writable = WritableGrant(dataSets = UidGrant(uids = listOf("ds1"))),
                    )
                },
            )

        assertEquals(UidScope.Only(setOf("ds1", "ds2")), mapped.dataSets)
        assertEquals(UidScope.Only(setOf("ds1")), mapped.writable.dataSets)
        // And the effective writable set is the intersection, so writable-but-unreadable is empty.
        assertEquals(UidScope.Only(setOf("ds1")), mapped.writableDataSets())
    }

    @Test
    fun `a writable entry outside the read grant grants nothing`() {
        val mapped =
            map(
                scope {
                    copy(
                        dataSets = UidGrant(uids = listOf("ds1")),
                        writable = WritableGrant(dataSets = UidGrant(uids = listOf("other"))),
                    )
                },
            )

        assertEquals(UidScope.Only(emptySet()), mapped.writableDataSets())
    }

    @Test
    fun `a config with no scope block keeps the behaviour it used to get`() {
        // Existing dataStore entries predate the scope block; they must not silently lose access.
        @Suppress("DEPRECATION")
        val legacy =
            PluginMetadata(
                id = "org.legacy",
                version = "1.0.0",
                entryPoint = "org.legacy.Entry",
                allowedProgramUids = listOf("p1"),
                allowedDataSetUids = listOf("ds1"),
            )

        val mapped = legacy.effectiveScope.toD2DataScope(legacy.id)

        assertEquals(UidScope.Only(setOf("p1")), mapped.programs)
        assertEquals(UidScope.Only(setOf("ds1")), mapped.dataSets)
        // There was no org unit scoping before, so imposing one now would break existing plugins.
        assertEquals(OrgUnitScope.All, mapped.orgUnits)
        assertTrue(mapped.has(D2Capability.READ_TRACKED_ENTITY))
        assertTrue(mapped.has(D2Capability.WRITE_DATA_VALUE))
        // But only data values were ever writable, so nothing else becomes writable now.
        assertEquals(UidScope.Only(setOf("ds1")), mapped.writableDataSets())
        assertEquals(UidScope.None, mapped.writable.programs)
    }

    @Test
    fun `an explicit scope block takes precedence over the legacy fields`() {
        @Suppress("DEPRECATION")
        val metadata =
            PluginMetadata(
                id = "org.test",
                version = "1.0.0",
                entryPoint = "org.test.Entry",
                scope =
                    PluginScope(
                        programs = UidGrant(uids = listOf("new")),
                        capabilities = listOf(PluginCapability.READ_EVENT.name),
                    ),
                allowedProgramUids = listOf("legacy"),
            )

        val mapped = metadata.effectiveScope.toD2DataScope(metadata.id)

        assertEquals(UidScope.Only(setOf("new")), mapped.programs)
        assertEquals(setOf(D2Capability.READ_EVENT), mapped.capabilities)
    }
}
