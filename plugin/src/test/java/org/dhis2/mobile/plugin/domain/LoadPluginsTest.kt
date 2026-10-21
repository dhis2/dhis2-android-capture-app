package org.dhis2.mobile.plugin.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.data.AppHubRepository
import org.dhis2.mobile.plugin.data.CaptureConfigRepository
import org.dhis2.mobile.plugin.data.CustomFormRepository
import org.dhis2.mobile.plugin.data.PluginAppRepository
import org.dhis2.mobile.plugin.data.PluginBundleInstaller
import org.dhis2.mobile.plugin.domain.model.FormFieldPluginPlacement
import org.dhis2.mobile.plugin.domain.model.FormLayout
import org.dhis2.mobile.plugin.domain.model.FormLayoutElement
import org.dhis2.mobile.plugin.domain.model.FormLayoutSection
import org.dhis2.mobile.plugin.domain.model.PluginApp
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LoadPluginsTest {
    @get:Rule
    val temp = TemporaryFolder()

    private val captureConfig: CaptureConfigRepository = mock()
    private val apps: PluginAppRepository = mock()
    private val appHub: AppHubRepository = mock()
    private val registry = PluginRegistry()
    private val customForms: CustomFormRepository = mock { onBlocking { refresh() } doReturn Result.success(Unit) }
    private val installer by lazy {
        val unconfined = Dispatchers.Unconfined
        PluginBundleInstaller(temp.newFolder("web-plugins"), Dispatcher(unconfined, unconfined, unconfined))
    }
    private val loadPlugins by lazy { LoadPlugins(captureConfig, apps, appHub, installer, registry, customForms) }

    private val placement =
        FormFieldPluginPlacement(
            formUid = "WZbXY0S00lP",
            elementId = "matrix-allergies",
            pluginSource = "http://localhost:8080/api/apps/matrix-select/plugin.html",
            fields = emptyList(),
        )
    private val app = PluginApp(key = "matrix-select", name = "Matrix Select", version = "1.0.2", appHubId = null)
    private val entryScript = "console.log('matrix')".toByteArray()
    private val bundle =
        ByteArrayOutputStream()
            .also { out ->
                ZipOutputStream(out).use { zip ->
                    zip.putNextEntry(ZipEntry("plugin.html"))
                    zip.write("""<script type="module" src="./assets/plugin-a.js"></script>""".toByteArray())
                    zip.closeEntry()
                    zip.putNextEntry(ZipEntry("assets/plugin-a.js"))
                    zip.write(entryScript)
                    zip.closeEntry()
                }
            }.toByteArray()

    private fun layoutWith(placement: FormFieldPluginPlacement) =
        FormLayout(placement.formUid, listOf(FormLayoutSection("section", null, listOf(FormLayoutElement.Plugin(placement)))))

    private suspend fun givenOnline() {
        whenever(captureConfig.refresh()) doReturn Result.success(Unit)
        whenever(captureConfig.formLayouts()) doReturn Result.success(listOf(layoutWith(placement)))
        whenever(apps.installedApp("matrix-select")) doReturn Result.success(app)
        whenever(apps.serverFile("matrix-select", "assets/plugin-a.js")) doReturn entryScript
        whenever(appHub.downloadUrl(app)) doReturn Result.success("https://apps.dhis2.org/x.zip")
        whenever(appHub.download("https://apps.dhis2.org/x.zip")) doReturn Result.success(bundle)
    }

    @Test
    fun `downloads the App Hub build the server runs, installs it and registers its placements`() =
        runTest {
            givenOnline()

            assertTrue(loadPlugins(Unit).isSuccess)

            val state = registry.state.value
            assertEquals(listOf("matrix-select"), state.plugins.keys.toList())
            assertEquals("1.0.2", state.plugins.getValue("matrix-select").version)
            assertEquals(listOf(layoutWith(placement)), state.layouts.values.toList())
        }

    @Test
    fun `refreshes the data set custom forms, which the SDK does not sync`() =
        runTest {
            givenOnline()

            loadPlugins(Unit)

            verify(customForms).refresh()
        }

    @Test
    fun `an installed version is reused without downloading again`() =
        runTest {
            givenOnline()
            loadPlugins(Unit)

            loadPlugins(Unit)

            verify(appHub, times(1)).download(any())
        }

    @Test
    fun `a bundle that differs from what the server runs is not installed`() =
        runTest {
            givenOnline()
            whenever(apps.serverFile("matrix-select", "assets/plugin-a.js")) doReturn "tampered".toByteArray()

            loadPlugins(Unit)

            assertTrue(
                registry.state.value.plugins
                    .isEmpty(),
            )
        }

    @Test
    fun `offline, the cached configuration and the last installed bundle are used`() =
        runTest {
            givenOnline()
            loadPlugins(Unit)
            whenever(captureConfig.refresh()) doReturn Result.failure(RuntimeException("offline"))
            whenever(apps.installedApp("matrix-select")) doReturn Result.failure(RuntimeException("offline"))
            registry.clear()

            loadPlugins(Unit)

            assertEquals(
                "1.0.2",
                registry.state.value.plugins
                    .getValue("matrix-select")
                    .version,
            )
        }

    @Test
    fun `a plugin not installed on the server is skipped`() =
        runTest {
            givenOnline()
            whenever(apps.installedApp("matrix-select")) doReturn Result.failure(NoSuchElementException("not installed"))

            loadPlugins(Unit)

            verify(appHub, never()).download(any())
            assertTrue(
                registry.state.value.plugins
                    .isEmpty(),
            )
        }

    @Test
    fun `a source that is not an installed app is ignored`() =
        runTest {
            givenOnline()
            whenever(captureConfig.formLayouts()) doReturn
                Result.success(listOf(layoutWith(placement.copy(pluginSource = "http://localhost:3001/plugin.html"))))

            assertTrue(loadPlugins(Unit).isSuccess)

            verify(apps, never()).installedApp(any())
        }
}
