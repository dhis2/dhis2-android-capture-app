package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * [PluginMetadata] is the wire format of the server dataStore config
 * (`dhis2AndroidPlugins/config`), so its deserialization *is* the contract with DHIS2
 * administrators. These tests pin that contract: what an admin can omit, what they cannot
 * get wrong silently, and that a typo fails loudly rather than granting the wrong scope.
 */
class PluginMetadataTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes the config documented for administrators`() {
        val decoded =
            json.decodeFromString<PluginMetadata>(
                """
                {
                  "id": "org.myorg.my-plugin",
                  "version": "1.0.0",
                  "entryPoint": "org.myorg.plugin.MyPlugin",
                  "downloadUrl": "https://example.com/my-plugin-1.0.0.zip",
                  "checksum": "sha256:abc123",
                  "allowedProgramUids": ["UID1"],
                  "allowedDataSetUids": [],
                  "injectionPoints": ["HOME_ABOVE_PROGRAM_LIST"]
                }
                """.trimIndent(),
            )

        assertEquals("org.myorg.my-plugin", decoded.id)
        assertEquals("1.0.0", decoded.version)
        assertEquals("org.myorg.plugin.MyPlugin", decoded.entryPoint)
        assertEquals("https://example.com/my-plugin-1.0.0.zip", decoded.downloadUrl)
        assertEquals("sha256:abc123", decoded.checksum)
        assertEquals(listOf("UID1"), decoded.allowedProgramUids)
        assertEquals(emptyList(), decoded.allowedDataSetUids)
        assertEquals(listOf(InjectionPoint.HOME_ABOVE_PROGRAM_LIST), decoded.injectionPoints)
    }

    @Test
    fun `only id version and entryPoint are required`() {
        val decoded =
            json.decodeFromString<PluginMetadata>(
                """{"id":"a","version":"1","entryPoint":"E"}""",
            )

        assertEquals(emptyList(), decoded.allowedProgramUids)
        assertEquals(emptyList(), decoded.allowedDataSetUids)
        assertEquals(emptyList(), decoded.injectionPoints)
        assertEquals("", decoded.downloadUrl)
        assertEquals("", decoded.checksum)
    }

    @Test
    fun `a plugin with no allow-lists is granted no data scope`() {
        // The default must be deny-all: an admin who forgets the allow-lists should get a
        // plugin that can render but not read, never one with implicit access.
        val decoded =
            json.decodeFromString<PluginMetadata>(
                """{"id":"a","version":"1","entryPoint":"E"}""",
            )

        assertTrue(decoded.allowedProgramUids.isEmpty())
        assertTrue(decoded.allowedDataSetUids.isEmpty())
    }

    @Test
    fun `missing a required field fails rather than defaulting`() {
        assertFailsWith<SerializationException> {
            json.decodeFromString<PluginMetadata>("""{"version":"1","entryPoint":"E"}""")
        }
    }

    @Test
    fun `an unknown injection point fails loudly`() {
        // A typo in the admin's config must not be silently dropped - that would render the
        // plugin nowhere with no explanation.
        assertFailsWith<SerializationException> {
            json.decodeFromString<PluginMetadata>(
                """{"id":"a","version":"1","entryPoint":"E","injectionPoints":["HOME_ABOVE_PROGAM_LIST"]}""",
            )
        }
    }

    @Test
    fun `unknown keys are tolerated so newer configs still load on older apps`() {
        val decoded =
            json.decodeFromString<PluginMetadata>(
                """{"id":"a","version":"1","entryPoint":"E","futureField":"ignored"}""",
            )

        assertEquals("a", decoded.id)
    }

    @Test
    fun `survives a round trip`() {
        val original =
            PluginMetadata(
                id = "org.dhis2.myplugin",
                version = "1.5.0",
                entryPoint = "org.dhis2.pluginimplementationtest.MyPlugin",
                allowedProgramUids = listOf("IpHINAT79UW"),
                allowedDataSetUids = listOf("BfMAe6Itzgt"),
                injectionPoints = listOf(InjectionPoint.HOME_ABOVE_PROGRAM_LIST),
                downloadUrl = "http://10.0.2.2:8081/plugin-1.5.0.zip",
                checksum = "sha256:deadbeef",
            )

        assertEquals(original, json.decodeFromString(json.encodeToString(original)))
    }

    @Test
    fun `injection point names are part of the server contract`() {
        // Renaming or reordering these breaks every deployed dataStore config, so the
        // serialized names are pinned here deliberately.
        assertEquals(
            listOf("HOME_ABOVE_PROGRAM_LIST"),
            InjectionPoint.entries.map { it.name },
        )
    }
}
