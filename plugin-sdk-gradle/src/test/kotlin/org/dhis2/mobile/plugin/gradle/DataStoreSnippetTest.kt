package org.dhis2.mobile.plugin.gradle

import org.junit.Assert.assertEquals
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

    private fun render(bundleFileName: String = "plugin-1.5.0.zip") =
        DataStoreSnippet.render(
            pluginId = "org.dhis2.pluginimplementationtest",
            version = "1.5.0",
            entryPoint = "org.dhis2.pluginimplementationtest.MyPlugin",
            bundleFileName = bundleFileName,
            checksum = "sha256:abc123",
        )
}
