package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.ListFilterConnector
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.datastore.DataStoreCollectionRepository
import org.hisp.dhis.android.core.datastore.DataStoreDownloader
import org.hisp.dhis.android.core.datastore.DataStoreEntry
import org.hisp.dhis.android.core.datastore.DataStoreModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Reads the plugin config from the server dataStore (`dhis2AndroidPlugins/config`).
 *
 * The "no config" cases matter most: this used to fall back to a hardcoded plugin, so these
 * tests exist to keep that from creeping back. A device with no config must load nothing.
 */
class AppHubPluginRepositoryTest {
    // Stubbed explicitly rather than with RETURNS_DEEP_STUBS: the filter connectors are
    // generic in `R : BaseRepository`, so a deep stub hands back a mock of the erased base
    // type and the chain fails with a ClassCastException before reaching blockingGet().
    private val collectionRepository: DataStoreCollectionRepository = mock()
    private val namespaceFilter: StringFilterConnector<DataStoreCollectionRepository> = mock()
    private val keyFilter: StringFilterConnector<DataStoreCollectionRepository> = mock()
    private val dataStoreModule: DataStoreModule = mock()
    private val d2: D2 = mock()

    private val downloader: DataStoreDownloader = mock()
    private val downloadFilter: ListFilterConnector<DataStoreDownloader, String> = mock()

    @Before
    fun setUp() {
        whenever(d2.dataStoreModule()).thenReturn(dataStoreModule)
        whenever(dataStoreModule.dataStore()).thenReturn(collectionRepository)
        whenever(collectionRepository.byNamespace()).thenReturn(namespaceFilter)
        whenever(namespaceFilter.eq(any())).thenReturn(collectionRepository)
        whenever(collectionRepository.byKey()).thenReturn(keyFilter)
        whenever(keyFilter.eq(any())).thenReturn(collectionRepository)

        whenever(dataStoreModule.dataStoreDownloader()).thenReturn(downloader)
        whenever(downloader.byNamespace()).thenReturn(downloadFilter)
        whenever(downloadFilter.eq(any())).thenReturn(downloader)
    }

    private fun givenDataStoreReturns(entries: List<DataStoreEntry>) {
        whenever(collectionRepository.blockingGet()).thenReturn(entries)
    }

    private fun givenDataStoreFails(error: Throwable) {
        whenever(collectionRepository.blockingGet()).thenThrow(error)
    }

    private fun entry(value: String?): DataStoreEntry = mock<DataStoreEntry>().also { whenever(it.value()).thenReturn(value) }

    // Injected so the test controls threading instead of hopping to the real IO pool.
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository
        get() =
            AppHubPluginRepository(
                d2,
                Dispatcher(io = testDispatcher, main = testDispatcher, default = testDispatcher),
            )

    // ── No configuration ──────────────────────────────────────────────────────

    @Test
    fun `no dataStore entry yields no plugins`() =
        runTest {
            givenDataStoreReturns(emptyList())

            val result = repository.getConfiguredPlugins()

            assertTrue(result.isSuccess)
            assertEquals(emptyList<PluginMetadata>(), result.getOrThrow())
        }

    @Test
    fun `an entry with a null value yields no plugins`() =
        runTest {
            givenDataStoreReturns(listOf(entry(null)))

            assertEquals(emptyList<PluginMetadata>(), repository.getConfiguredPlugins().getOrThrow())
        }

    @Test
    fun `an entry with a blank value yields no plugins`() =
        runTest {
            givenDataStoreReturns(listOf(entry("   ")))

            assertEquals(emptyList<PluginMetadata>(), repository.getConfiguredPlugins().getOrThrow())
        }

    @Test
    fun `an empty plugins array yields no plugins`() =
        runTest {
            givenDataStoreReturns(listOf(entry("""{"plugins":[]}""")))

            assertEquals(emptyList<PluginMetadata>(), repository.getConfiguredPlugins().getOrThrow())
        }

    @Test
    fun `missing configuration is a success not a failure`() =
        runTest {
            // The load pipeline treats a failure as "something went wrong"; an unconfigured
            // server is the normal case and must not be reported as an error.
            givenDataStoreReturns(emptyList())

            assertTrue(repository.getConfiguredPlugins().isSuccess)
        }

    // ── Valid configuration ───────────────────────────────────────────────────

    @Test
    fun `parses the config an administrator writes`() =
        runTest {
            givenDataStoreReturns(
                listOf(
                    entry(
                        """
                        {
                          "plugins": [
                            {
                              "id": "org.dhis2.myplugin",
                              "version": "1.5.0",
                              "entryPoint": "org.myorg.myplugin.MyPlugin",
                              "downloadUrl": "http://10.0.2.2:8081/plugin-1.5.0.zip",
                              "checksum": "sha256:deadbeef",
                              "allowedProgramUids": ["IpHINAT79UW"],
                              "allowedDataSetUids": [],
                              "injectionPoints": ["HOME_ABOVE_PROGRAM_LIST"]
                            }
                          ]
                        }
                        """.trimIndent(),
                    ),
                ),
            )

            val plugin = repository.getConfiguredPlugins().getOrThrow().single()

            assertEquals("org.dhis2.myplugin", plugin.id)
            assertEquals("1.5.0", plugin.version)
            assertEquals("org.myorg.myplugin.MyPlugin", plugin.entryPoint)
            assertEquals("http://10.0.2.2:8081/plugin-1.5.0.zip", plugin.downloadUrl)
            assertEquals("sha256:deadbeef", plugin.checksum)
            assertEquals(listOf("IpHINAT79UW"), plugin.allowedProgramUids)
            assertEquals(listOf(InjectionPoint.HOME_ABOVE_PROGRAM_LIST), plugin.injectionPoints)
        }

    @Test
    fun `parses several plugins in order`() =
        runTest {
            givenDataStoreReturns(
                listOf(
                    entry(
                        """
                        {"plugins":[
                          {"id":"org.a","version":"1","entryPoint":"A"},
                          {"id":"org.b","version":"2","entryPoint":"B"}
                        ]}
                        """.trimIndent(),
                    ),
                ),
            )

            assertEquals(
                listOf("org.a", "org.b"),
                repository.getConfiguredPlugins().getOrThrow().map { it.id },
            )
        }

    @Test
    fun `ignores keys it does not recognise`() =
        runTest {
            // Lets a newer server config load on an older app build.
            givenDataStoreReturns(
                listOf(
                    entry(
                        """{"schemaVersion":2,"plugins":[{"id":"org.a","version":"1","entryPoint":"A","future":true}]}""",
                    ),
                ),
            )

            assertEquals(
                "org.a",
                repository
                    .getConfiguredPlugins()
                    .getOrThrow()
                    .single()
                    .id,
            )
        }

    // ── Malformed configuration ───────────────────────────────────────────────

    @Test
    fun `malformed json is reported as a failure`() =
        runTest {
            givenDataStoreReturns(listOf(entry("{ not json")))

            assertTrue(repository.getConfiguredPlugins().isFailure)
        }

    @Test
    fun `a plugin entry missing a required field is reported as a failure`() =
        runTest {
            // Better to surface the admin's mistake than to skip the plugin silently.
            givenDataStoreReturns(listOf(entry("""{"plugins":[{"version":"1","entryPoint":"A"}]}""")))

            assertTrue(repository.getConfiguredPlugins().isFailure)
        }

    @Test
    fun `a dataStore read failure is reported as a failure`() =
        runTest {
            givenDataStoreFails(RuntimeException("database unavailable"))

            assertTrue(repository.getConfiguredPlugins().isFailure)
        }

    // ── Refreshing the namespace ──────────────────────────────────────────────

    @Test
    fun `downloads the plugin namespace before reading it`() =
        runTest {
            // The SDK dataStore is a local mirror and nothing else in the app downloads it,
            // so without this refresh the read can only ever return what a previous run cached.
            givenDataStoreReturns(emptyList())

            repository.refreshConfiguration()

            verify(downloadFilter).eq("dhis2AndroidPlugins")
            verify(downloader).blockingDownload()
        }

    @Test
    fun `downloads only the plugin namespace`() =
        runTest {
            // Pulling the whole dataStore would drag in namespaces that are none of the
            // plugin system's business and can be large.
            givenDataStoreReturns(emptyList())

            repository.refreshConfiguration()

            verify(downloader).byNamespace()
            verify(downloadFilter, never()).eq(argThat { this != "dhis2AndroidPlugins" })
        }

    @Test
    fun `a failed refresh still returns the cached config`() =
        runTest {
            // Offline-first: losing the network must not disable already-configured plugins.
            whenever(downloader.blockingDownload()).thenThrow(RuntimeException("offline"))
            givenDataStoreReturns(listOf(entry("""{"plugins":[{"id":"org.a","version":"1","entryPoint":"A"}]}""")))

            assertTrue(repository.refreshConfiguration().isFailure)
            assertEquals(
                "org.a",
                repository
                    .getConfiguredPlugins()
                    .getOrThrow()
                    .single()
                    .id,
            )
        }

    @Test
    fun `a failed refresh does not break reading`() =
        runTest {
            whenever(downloader.blockingDownload()).thenThrow(RuntimeException("offline"))
            givenDataStoreReturns(emptyList())

            // The refresh reports the failure; reading is unaffected.
            assertTrue(repository.refreshConfiguration().isFailure)
            assertTrue(repository.getConfiguredPlugins().isSuccess)
        }
}
