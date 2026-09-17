package org.dhis2.mobile.plugin.gradle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataStoreSnippetTest {
    @Test
    fun `renders the configured identity, version and checksum`() {
        assertEquals(
            """
            {
              "plugins": [
                {
                  "id": "org.dhis2.pluginimplementationtest",
                  "version": "1.5.0",
                  "entryPoint": "org.dhis2.pluginimplementationtest.MyPlugin",
                  "downloadUrl": "http://10.0.2.2:8081/plugin-1.5.0.zip",
                  "checksum": "sha256:abc123",
                  "injectionPoints": [
                    "HOME_ABOVE_PROGRAM_LIST"
                  ]
                }
              ]
            }

            """.trimIndent(),
            render(),
        )
    }

    @Test
    fun `the emulator download url is named after the bundle file`() {
        // The host locates the bundle by this url alone, so it tracks bundleFileName rather than
        // being reassembled from the id and version — the served name need not encode either.
        val snippet = render(bundleFileName = "renamed.zip")

        assertTrue(snippet.contains(""""downloadUrl": "http://10.0.2.2:8081/renamed.zip""""))
    }

    @Test
    fun `renders a replacement slot with the data sets it applies to`() {
        assertEquals(
            """
            {
              "plugins": [
                {
                  "id": "org.dhis2.pluginimplementationtest",
                  "version": "1.5.0",
                  "entryPoint": "org.dhis2.pluginimplementationtest.MyPlugin",
                  "downloadUrl": "http://10.0.2.2:8081/plugin-1.5.0.zip",
                  "checksum": "sha256:abc123",
                  "injectionPoints": [
                    "DATA_SET_INSTANCE_CONTENT"
                  ],
                  "slotConfig": {
                    "DATA_SET_INSTANCE_CONTENT": {
                      "dataSetUids": ["lyLU2wR22tC", "BfMAe6Itzgt"]
                    }
                  }
                }
              ]
            }

            """.trimIndent(),
            render(
                injectionPoints = listOf("DATA_SET_INSTANCE_CONTENT"),
                slotConfig =
                    mapOf(
                        "DATA_SET_INSTANCE_CONTENT" to
                            mapOf("dataSetUids" to listOf("lyLU2wR22tC", "BfMAe6Itzgt")),
                    ),
            ),
        )
    }

    @Test
    fun `several slots are comma separated`() {
        val snippet =
            render(
                injectionPoints = listOf("HOME_ABOVE_PROGRAM_LIST", "DATA_SET_INSTANCE_CONTENT"),
            )

        assertTrue(
            snippet.contains(
                listOf(
                    """      "injectionPoints": [""",
                    """        "HOME_ABOVE_PROGRAM_LIST",""",
                    """        "DATA_SET_INSTANCE_CONTENT"""",
                    "      ]",
                ).joinToString("\n"),
            ),
        )
    }

    @Test
    fun `an unconfigured slot leaves the block out rather than emitting it empty`() {
        // A replacement slot renders nowhere until it is configured, so an empty block would read
        // like a working configuration that happens to do nothing.
        assertFalse(render().contains("slotConfig"))
    }

    private fun render(
        bundleFileName: String = "plugin-1.5.0.zip",
        injectionPoints: List<String> = listOf("HOME_ABOVE_PROGRAM_LIST"),
        slotConfig: Map<String, Map<String, List<String>>> = emptyMap(),
    ) = DataStoreSnippet.render(
        pluginId = "org.dhis2.pluginimplementationtest",
        version = "1.5.0",
        entryPoint = "org.dhis2.pluginimplementationtest.MyPlugin",
        bundleFileName = bundleFileName,
        checksum = "sha256:abc123",
        injectionPoints = injectionPoints,
        slotConfig = slotConfig,
    )
}
