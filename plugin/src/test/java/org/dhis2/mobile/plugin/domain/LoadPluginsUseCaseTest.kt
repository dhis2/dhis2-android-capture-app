package org.dhis2.mobile.plugin.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.plugin.data.AppHubPluginRepository
import org.dhis2.mobile.plugin.data.LoadedPlugin
import org.dhis2.mobile.plugin.data.PluginDownloader
import org.dhis2.mobile.plugin.data.PluginLoader
import org.dhis2.mobile.plugin.data.PluginVerifier
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.dhis2.mobile.plugin.security.HostDhis2PluginContextFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.koin.dsl.module
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Covers the plugin load pipeline: the API-level guard, and — via the injected
 * [LoadPluginsUseCase] api level — the download → verify → load → register path that runs on a
 * supported device.
 *
 * The real [PluginRegistry] is used rather than a mock, so "was it registered" is asserted against
 * the state the render path actually reads instead of against a call.
 */
class LoadPluginsUseCaseTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val repository: AppHubPluginRepository = mock()
    private val downloader: PluginDownloader = mock()
    private val verifier: PluginVerifier = mock()
    private val loader: PluginLoader = mock()
    private val registry = PluginRegistry()
    private val contextFactory: HostDhis2PluginContextFactory = mock()

    /** API 26 is the floor `InMemoryDexClassLoader` requires. */
    private fun useCase(apiLevel: Int = 26) =
        LoadPluginsUseCase(
            appHubPluginRepository = repository,
            pluginDownloader = downloader,
            pluginVerifier = verifier,
            pluginLoader = loader,
            pluginRegistry = registry,
            contextFactory = contextFactory,
            deviceApiLevel = apiLevel,
        )

    private class FakePlugin(
        private val koinModule: org.koin.core.module.Module? = null,
    ) : Dhis2Plugin {
        override fun provideKoinModule() = koinModule

        @androidx.compose.runtime.Composable
        override fun content(context: Dhis2PluginContext) = Unit
    }

    private fun metadata(id: String = "org.a") =
        PluginMetadata(
            id = id,
            version = "1.0.0",
            entryPoint = "$id.Entry",
            injectionPoints = listOf(InjectionPoint.HOME_ABOVE_PROGRAM_LIST),
            downloadUrl = "http://host/$id.zip",
            checksum = "sha256:abc",
        )

    /**
     * Stubs the whole pipeline green for [metas] and returns the [LoadedPlugin] minted for each,
     * so a test can assert that exactly those instances reached the registry.
     */
    private suspend fun stubHappyPath(vararg metas: PluginMetadata): Map<String, LoadedPlugin> {
        whenever(repository.refreshConfiguration()).thenReturn(Result.success(Unit))
        whenever(repository.getConfiguredPlugins()).thenReturn(Result.success(metas.toList()))
        whenever(verifier.verify(any(), any())).thenReturn(true)
        whenever(verifier.verifySignature(any())).thenReturn(Result.success(Unit))

        return metas.associate { meta ->
            val bundle = tempFolder.newFile("${meta.id}.zip").apply { writeBytes(byteArrayOf(1, 2, 3)) }
            whenever(downloader.getOrDownload(meta)).thenReturn(Result.success(bundle))

            val loaded =
                LoadedPlugin(
                    plugin = FakePlugin(),
                    resourceRoot = tempFolder.newFolder(meta.id),
                    classLoader = javaClass.classLoader!!,
                )
            whenever(loader.load(bundle, meta)).thenReturn(loaded)
            whenever(contextFactory.create(meta)).thenReturn(mock<Dhis2PluginContext>())
            meta.id to loaded
        }
    }

    // ---------------------------------------------------------------- the API-level guard

    @Test
    fun `succeeds on a device below API 26 instead of failing`() =
        runTest {
            // A device that cannot load plugins is not a broken device; login must proceed.
            assertTrue(useCase(apiLevel = 25)().isSuccess)
        }

    @Test
    fun `loads nothing on a device below API 26`() =
        runTest {
            useCase(apiLevel = 25)()

            assertTrue(registry.plugins.value.isEmpty())
        }

    @Test
    fun `does not even ask the server for config on an unsupported device`() =
        runTest {
            // No point spending a dataStore read when nothing could be loaded from it.
            useCase(apiLevel = 25)()

            verifyNoInteractions(repository)
        }

    @Test
    fun `touches no part of the pipeline on an unsupported device`() =
        runTest {
            useCase(apiLevel = 25)()

            verifyNoInteractions(downloader)
            verifyNoInteractions(verifier)
            verifyNoInteractions(loader)
            verifyNoInteractions(contextFactory)
        }

    @Test
    fun `API 26 exactly is supported`() =
        runTest {
            // Pins the boundary: the floor is the loader's own minimum, not one above it.
            stubHappyPath(metadata())

            useCase(apiLevel = 26)()

            assertEquals(listOf("org.a"), registry.plugins.value.map { it.metadata.id })
        }

    // ---------------------------------------------------------------- the happy path

    @Test
    fun `loads and registers a configured plugin`() =
        runTest {
            val loaded = stubHappyPath(metadata()).getValue("org.a")

            val result = useCase()()

            assertTrue(result.isSuccess)
            val registered = registry.plugins.value.single()
            assertSame(loaded.plugin, registered.plugin)
            assertEquals("org.a", registered.metadata.id)
            assertEquals(loaded.resourceRoot, registered.resourceRoot)
        }

    @Test
    fun `registers the class loader that defined the plugin`() =
        runTest {
            // The render path keys its composition on this; a wrong loader resurfaces as the
            // ClassCastException that keying was added to fix.
            val loaded = stubHappyPath(metadata()).getValue("org.a")

            useCase()()

            assertSame(
                loaded.classLoader,
                registry.plugins.value
                    .single()
                    .classLoader,
            )
        }

    @Test
    fun `hands the plugin the context built from its own server metadata`() =
        runTest {
            val meta = metadata()
            val context = mock<Dhis2PluginContext>()
            stubHappyPath(meta)
            whenever(contextFactory.create(meta)).thenReturn(context)

            useCase()()

            verify(contextFactory).create(meta)
            assertSame(
                context,
                registry.plugins.value
                    .single()
                    .context,
            )
        }

    @Test
    fun `gives each plugin a container of its own`() =
        runTest {
            // Two plugins must not share a container, or one's binding is visible to the other.
            stubHappyPath(metadata("org.a"), metadata("org.b"))

            useCase()()

            val containers = registry.plugins.value.map { it.koinApplication }
            assertEquals(2, containers.size)
            assertNotSame(containers[0], containers[1])
        }

    @Test
    fun `loads the plugin's own Koin module into its container`() =
        runTest {
            val meta = metadata()
            stubHappyPath(meta)
            val plugin = FakePlugin(module { single { "from-the-plugin" } })
            whenever(loader.load(any(), eq(meta)))
                .thenReturn(
                    LoadedPlugin(plugin, tempFolder.newFolder("res-b"), javaClass.classLoader!!),
                )

            useCase()()

            val container =
                registry.plugins.value
                    .single()
                    .koinApplication
            assertEquals("from-the-plugin", container.koin.get<String>())
        }

    @Test
    fun `refreshes the configuration before reading it`() =
        runTest {
            // A read alone would only ever see the previous run's cache.
            stubHappyPath(metadata())

            useCase()()

            inOrder(repository) {
                verify(repository).refreshConfiguration()
                verify(repository).getConfiguredPlugins()
            }
            // Pinned exactly once: `inOrder` alone is satisfied by a *later* read, so without this
            // an added read before the refresh would slip through.
            verify(repository, times(1)).getConfiguredPlugins()
        }

    // ---------------------------------------------------------------- degraded and failing paths

    @Test
    fun `carries on with the cached config when the refresh fails`() =
        runTest {
            // An offline device still runs the plugins it already has.
            stubHappyPath(metadata())
            whenever(repository.refreshConfiguration())
                .thenReturn(Result.failure(IOException("offline")))

            val result = useCase()()

            assertTrue(result.isSuccess)
            assertEquals(1, registry.plugins.value.size)
        }

    @Test
    fun `succeeds with nothing loaded when the config cannot be read`() =
        runTest {
            whenever(repository.refreshConfiguration()).thenReturn(Result.success(Unit))
            whenever(repository.getConfiguredPlugins())
                .thenReturn(Result.failure(IOException("no dataStore")))

            val result = useCase()()

            assertTrue(result.isSuccess)
            assertTrue(registry.plugins.value.isEmpty())
            verifyNoInteractions(downloader)
        }

    @Test
    fun `does nothing when no plugins are configured`() =
        runTest {
            whenever(repository.refreshConfiguration()).thenReturn(Result.success(Unit))
            whenever(repository.getConfiguredPlugins()).thenReturn(Result.success(emptyList()))

            assertTrue(useCase()().isSuccess)
            assertTrue(registry.plugins.value.isEmpty())
            verifyNoInteractions(downloader)
        }

    @Test
    fun `evicts and skips a plugin that fails its checksum`() =
        runTest {
            val meta = metadata()
            stubHappyPath(meta)
            whenever(verifier.verify(any(), any())).thenReturn(false)

            val result = useCase()()

            assertTrue(result.isSuccess)
            assertTrue("a corrupt bundle must not be registered", registry.plugins.value.isEmpty())
            verify(downloader).evict(meta)
            // Cached-forever corruption is the failure mode eviction exists to prevent.
            verify(loader, never()).load(any(), any())
        }

    @Test
    fun `evicts and skips a plugin whose signature does not verify`() =
        runTest {
            val meta = metadata()
            stubHappyPath(meta)
            whenever(verifier.verifySignature(any()))
                .thenReturn(Result.failure(SecurityException("unsigned")))

            val result = useCase()()

            assertTrue(result.isSuccess)
            assertTrue("an unsigned bundle must not be registered", registry.plugins.value.isEmpty())
            verify(downloader).evict(meta)
            verify(loader, never()).load(any(), any())
        }

    @Test
    fun `a plugin that fails to download does not stop the next one`() =
        runTest {
            // The documented contract: each plugin's failure is isolated.
            val bad = metadata("org.bad")
            val good = metadata("org.good")
            stubHappyPath(bad, good)
            whenever(downloader.getOrDownload(bad)).thenReturn(Result.failure(IOException("404")))

            val result = useCase()()

            assertTrue(result.isSuccess)
            assertEquals(listOf("org.good"), registry.plugins.value.map { it.metadata.id })
        }

    @Test
    fun `a plugin that throws while loading does not stop the next one`() =
        runTest {
            val bad = metadata("org.bad")
            val good = metadata("org.good")
            stubHappyPath(bad, good)
            whenever(loader.load(any(), eq(bad))).thenThrow(RuntimeException("bad DEX"))

            val result = useCase()()

            assertTrue(result.isSuccess)
            assertEquals(listOf("org.good"), registry.plugins.value.map { it.metadata.id })
        }

    @Test
    fun `registers every configured plugin`() =
        runTest {
            stubHappyPath(metadata("org.a"), metadata("org.b"), metadata("org.c"))

            useCase()()

            assertEquals(
                listOf("org.a", "org.b", "org.c"),
                registry.plugins.value.map { it.metadata.id },
            )
        }
}
