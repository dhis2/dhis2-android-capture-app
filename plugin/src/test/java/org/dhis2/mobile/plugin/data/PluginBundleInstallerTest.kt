package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PluginBundleInstallerTest {
    @get:Rule
    val temp = TemporaryFolder()

    private val root by lazy { temp.newFolder("web-plugins") }
    private val unconfined = Dispatchers.Unconfined
    private val installer by lazy {
        PluginBundleInstaller(root, Dispatcher(io = unconfined, main = unconfined, default = unconfined))
    }

    private fun zip(vararg entries: Pair<String, String>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            entries.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    @Test
    fun `extracts the bundle into a directory named after the app and version`() =
        runTest {
            val bundle = zip("plugin.html" to "<html/>", "assets/app.js" to "console.log(1)")

            val plugin = installer.install("matrix-select", "1.0.2", "plugin.html", bundle).getOrThrow()

            assertEquals(RegisteredPlugin("matrix-select", "1.0.2", "plugin.html", File(root, "matrix-select-1.0.2")), plugin)
            assertEquals("console.log(1)", File(plugin.bundleDir, "assets/app.js").readText())
        }

    @Test
    fun `a bundle without its entry point is rejected`() =
        runTest {
            assertTrue(installer.install("p", "1", "plugin.html", zip("index.html" to "<html/>")).isFailure)
            assertFalse(File(root, "p-1").exists())
        }

    @Test
    fun `an entry escaping the bundle directory is rejected`() =
        runTest {
            val bundle = zip("plugin.html" to "<html/>", "../../evil.js" to "boom")

            assertTrue(installer.install("p", "1", "plugin.html", bundle).isFailure)
            assertFalse(File(root.parentFile, "evil.js").exists())
        }

    @Test
    fun `knows which version of an app is already installed`() =
        runTest {
            installer.install("p", "1", "plugin.html", zip("plugin.html" to "x")).getOrThrow()

            assertEquals("1", installer.installed("p", "1")?.version)
            assertNull(installer.installed("p", "2"))
        }

    @Test
    fun `offline, the bundle installed last is still found by app key alone`() =
        runTest {
            installer.install("p", "1", "plugin.html", zip("plugin.html" to "x")).getOrThrow()

            assertEquals("1", installer.installedAnyVersion("p")?.version)
            assertNull(installer.installedAnyVersion("other"))
        }

    @Test
    fun `installing a new version removes the previous one`() =
        runTest {
            installer.install("p", "1", "plugin.html", zip("plugin.html" to "v1")).getOrThrow()

            installer.install("p", "2", "plugin.html", zip("plugin.html" to "v2")).getOrThrow()

            assertFalse(File(root, "p-1").exists())
            assertEquals("2", installer.installedAnyVersion("p")?.version)
        }

    @Test
    fun `another app whose key shares a prefix is left alone`() =
        runTest {
            installer.install("p-extra", "1", "plugin.html", zip("plugin.html" to "x")).getOrThrow()

            installer.install("p", "1", "plugin.html", zip("plugin.html" to "y")).getOrThrow()

            assertTrue(File(root, "p-extra-1/plugin.html").exists())
        }
}
