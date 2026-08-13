package org.dhis2.mobile.plugin.security

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.dhis2.mobile.plugin.sdk.dto.DataValueDto
import org.hisp.dhis.android.core.D2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions

/**
 * The scope-enforcement half of the plugin security model.
 *
 * The contract from `docs/plugin-system.md` §6 is specific and worth pinning exactly:
 * out-of-scope access returns `Result.failure(SecurityException)` — *never* silently empty,
 * *never* thrown. These tests also assert D2 is never touched on a denied call, because an
 * enforcement check that runs after hitting the database has already leaked the data.
 */
class ScopedDhis2PluginContextTest {
    private val d2: D2 = mock()

    private fun metadata(
        programs: List<String> = emptyList(),
        dataSets: List<String> = emptyList(),
    ) = PluginMetadata(
        id = "org.myorg.my-plugin",
        version = "1.0.0",
        entryPoint = "org.myorg.MyPlugin",
        allowedProgramUids = programs,
        allowedDataSetUids = dataSets,
    )

    private fun context(metadata: PluginMetadata) = ScopedDhis2PluginContext(metadata, d2)

    private val dataValue =
        DataValueDto(
            dataElementUid = "DE_UID",
            value = "42",
            period = "202608",
            orgUnitUid = "OU_UID",
            categoryOptionComboUid = "COC_UID",
        )

    // ── Reads: programs ───────────────────────────────────────────────────────

    @Test
    fun `reading a program outside the allow-list fails with SecurityException`() =
        runTest {
            val result =
                context(metadata(programs = listOf("ALLOWED_UID")))
                    .getTrackedEntityInstances("OTHER_UID")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SecurityException)
        }

    @Test
    fun `a denied program read never reaches the database`() =
        runTest {
            context(metadata(programs = listOf("ALLOWED_UID")))
                .getTrackedEntityInstances("OTHER_UID")

            verifyNoInteractions(d2)
        }

    @Test
    fun `a plugin with no granted programs can read nothing`() =
        runTest {
            val result = context(metadata()).getTrackedEntityInstances("ANY_UID")

            assertTrue(result.isFailure)
            verifyNoInteractions(d2)
        }

    @Test
    fun `the denial names the plugin and the uid it asked for`() =
        runTest {
            val result =
                context(metadata(programs = listOf("ALLOWED_UID")))
                    .getTrackedEntityInstances("OTHER_UID")

            val message = result.exceptionOrNull()?.message.orEmpty()
            assertTrue("should name the plugin: $message", message.contains("org.myorg.my-plugin"))
            assertTrue("should name the requested uid: $message", message.contains("OTHER_UID"))
        }

    // ── Reads: data sets ──────────────────────────────────────────────────────

    @Test
    fun `reading a data set outside the allow-list fails with SecurityException`() =
        runTest {
            val result =
                context(metadata(dataSets = listOf("ALLOWED_DS")))
                    .getDataValues(orgUnitUid = "OU", dataSetUid = "OTHER_DS", period = "202608")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SecurityException)
            verifyNoInteractions(d2)
        }

    @Test
    fun `a program grant does not imply a data set grant`() =
        runTest {
            // Distinct allow-lists must stay distinct; granting a program must not widen
            // access to data sets.
            val result =
                context(metadata(programs = listOf("PROGRAM_UID")))
                    .getDataValues(orgUnitUid = "OU", dataSetUid = "PROGRAM_UID", period = "202608")

            assertTrue(result.isFailure)
            verifyNoInteractions(d2)
        }

    // ── Writes ────────────────────────────────────────────────────────────────

    @Test
    fun `writing to a data set outside the allow-list fails with SecurityException`() =
        runTest {
            val result =
                context(metadata(dataSets = listOf("ALLOWED_DS")))
                    .saveDataValue("OTHER_DS", dataValue)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SecurityException)
        }

    @Test
    fun `a denied write never reaches the database`() =
        runTest {
            context(metadata(dataSets = listOf("ALLOWED_DS")))
                .saveDataValue("OTHER_DS", dataValue)

            verifyNoInteractions(d2)
        }

    @Test
    fun `a read grant does not imply a write grant on another data set`() =
        runTest {
            val ctx = context(metadata(dataSets = listOf("READABLE_DS")))

            assertTrue(ctx.saveDataValue("OTHER_DS", dataValue).isFailure)
            verifyNoInteractions(d2)
        }

    // ── Shape of the failure ──────────────────────────────────────────────────

    @Test
    fun `denied access is a failed Result rather than a thrown exception`() =
        runTest {
            // Plugins run inside the host composition, so a thrown exception would take the
            // host down with it. The contract is an inert failed Result.
            val ctx = context(metadata())

            assertFalse(ctx.getTrackedEntityInstances("X").isSuccess)
            assertFalse(ctx.getDataValues("OU", "DS", "202608").isSuccess)
            assertFalse(ctx.saveDataValue("DS", dataValue).isSuccess)
        }

    @Test
    fun `denied access is never reported as an empty success`() =
        runTest {
            // An empty list would be indistinguishable from "no data", which would hide the
            // misconfiguration from the plugin author.
            val result = context(metadata()).getTrackedEntityInstances("X")

            assertNull(result.getOrNull())
        }

    @Test
    fun `metadata is exposed to the plugin unchanged`() =
        runTest {
            val meta = metadata(programs = listOf("P1"), dataSets = listOf("D1"))

            assertEquals(meta, context(meta).pluginMetadata)
        }

    @Test
    fun `the factory binds each context to its own plugin metadata`() {
        val factory = ScopedDhis2PluginContextFactory(d2)
        val first = metadata(programs = listOf("P1")).copy(id = "plugin.one")
        val second = metadata(programs = listOf("P2")).copy(id = "plugin.two")

        assertEquals(first, factory.create(first).pluginMetadata)
        assertEquals(second, factory.create(second).pluginMetadata)
    }
}
