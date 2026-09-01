package org.dhis2.mobile.plugin.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.plugin.data.AppHubPluginRepository
import org.dhis2.mobile.plugin.data.PluginDownloader
import org.dhis2.mobile.plugin.data.PluginLoader
import org.dhis2.mobile.plugin.data.PluginVerifier
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.security.HostDhis2PluginContextFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.Koin
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions

/**
 * Covers the API-level guard on the plugin load pipeline.
 *
 * `Build.VERSION.SDK_INT` reads as `0` from the stubbed `android.jar` used by JVM unit tests,
 * which is below the API 26 floor `InMemoryDexClassLoader` requires. That makes this the
 * *only* path reachable here — and it is worth pinning, because the documented behaviour on an
 * unsupported device is "succeed with zero plugins", not "fail".
 *
 * The download → verify → load → register pipeline cannot be exercised from a JVM unit test:
 * the guard short-circuits first, and `SDK_INT` is a `static final int` that cannot be
 * reassigned by reflection on JDK 17. Covering it needs either Robolectric (`@Config(sdk = 26)`)
 * or a small seam in production code to inject the API level. Neither is done here — see the
 * note in the PR rather than assuming the pipeline is tested.
 */
class LoadPluginsUseCaseTest {
    private val repository: AppHubPluginRepository = mock()
    private val downloader: PluginDownloader = mock()
    private val verifier: PluginVerifier = mock()
    private val loader: PluginLoader = mock()
    private val registry = PluginRegistry()
    private val contextFactory: HostDhis2PluginContextFactory = mock()
    private val koin: Koin = mock()

    private val useCase =
        LoadPluginsUseCase(
            appHubPluginRepository = repository,
            pluginDownloader = downloader,
            pluginVerifier = verifier,
            pluginLoader = loader,
            pluginRegistry = registry,
            contextFactory = contextFactory,
        )

    @Test
    fun `succeeds on a device below API 26 instead of failing`() =
        runTest {
            // A device that cannot load plugins is not a broken device; login must proceed.
            assertTrue(useCase().isSuccess)
        }

    @Test
    fun `loads nothing on a device below API 26`() =
        runTest {
            useCase()

            assertTrue(registry.plugins.value.isEmpty())
        }

    @Test
    fun `does not even ask the server for config on an unsupported device`() =
        runTest {
            // No point spending a dataStore read when nothing could be loaded from it.
            useCase()

            verifyNoInteractions(repository)
        }

    @Test
    fun `touches no part of the pipeline on an unsupported device`() =
        runTest {
            useCase()

            verifyNoInteractions(downloader)
            verifyNoInteractions(verifier)
            verifyNoInteractions(loader)
            verifyNoInteractions(koin)
        }
}
