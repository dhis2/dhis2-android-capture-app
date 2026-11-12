package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PluginBundleVerifierTest {
    private val pluginHtml =
        """
        <html><head>
          <meta name="dhis2-base-url" content="__DHIS2_BASE_URL__" />
          <script type="module" crossorigin src="./assets/plugin-ObS_TAuY.js"></script>
          <link rel="modulepreload" crossorigin href="./assets/index-D8AKK0o1.js">
        </head></html>
        """.trimIndent()

    private fun zip(vararg entries: Pair<String, ByteArray>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    private val entryScript = "console.log('matrix')".toByteArray()
    private val bundle = zip("plugin.html" to pluginHtml.toByteArray(), "assets/plugin-ObS_TAuY.js" to entryScript)

    @Test
    fun `finds the module script the entry page loads`() {
        assertEquals("assets/plugin-ObS_TAuY.js", PluginBundleVerifier.entryScriptPath(pluginHtml))
    }

    @Test
    fun `a page without a module script has no entry script`() {
        assertNull(PluginBundleVerifier.entryScriptPath("<html><body>hi</body></html>"))
    }

    @Test
    fun `a bundle whose entry script is the one the server serves is accepted`() =
        runTest {
            // plugin.html is not compared: the server rewrites it to inject its base URL.
            val result =
                PluginBundleVerifier.verify(bundle, "plugin.html") { path ->
                    entryScript.takeIf {
                        path ==
                            "assets/plugin-ObS_TAuY.js"
                    }
                }

            assertTrue(result.isSuccess)
        }

    @Test
    fun `a bundle the server is not running is rejected`() =
        runTest {
            val result = PluginBundleVerifier.verify(bundle, "plugin.html") { "something else".toByteArray() }

            assertTrue(result.isFailure)
        }

    @Test
    fun `a bundle that cannot be compared is rejected`() =
        runTest {
            val withoutScript = zip("plugin.html" to "<html></html>".toByteArray())

            assertTrue(PluginBundleVerifier.verify(withoutScript, "plugin.html") { entryScript }.isFailure)
            assertTrue(PluginBundleVerifier.verify(bundle, "plugin.html") { null }.isFailure)
        }
}
