package org.dhis2.mobile.plugin.domain

import android.os.Build
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.plugin.data.AppHubPluginRepository
import org.dhis2.mobile.plugin.data.PluginDownloader
import org.dhis2.mobile.plugin.data.PluginLoader
import org.dhis2.mobile.plugin.data.PluginVerifier
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.koin.core.Koin
import timber.log.Timber

/**
 * Downloads, verifies, loads, and registers all plugins configured on the DHIS2 server.
 *
 * This use case is intended to be called once at login time (after the server connection
 * is established). Each step is failure-isolated per plugin: a single plugin failing to
 * download, verify, or load does not block other plugins.
 *
 * Requires API 26+ for [android.system.Os.InMemoryDexClassLoader]. On older devices,
 * the use case completes successfully but loads zero plugins.
 */
class LoadPluginsUseCase(
    private val appHubPluginRepository: AppHubPluginRepository,
    private val pluginDownloader: PluginDownloader,
    private val pluginVerifier: PluginVerifier,
    private val pluginLoader: PluginLoader,
    private val pluginRegistry: PluginRegistry,
    private val koin: Koin,
) : UseCase<Unit, Unit> {

    override suspend fun invoke(input: Unit): Result<Unit> = runCatching {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Timber.w("Plugin system requires API 26+. Skipping plugin loading on this device.")
            return Result.success(Unit)
        }

        val metadataList = appHubPluginRepository.getConfiguredPlugins().getOrElse { err ->
            Timber.e(err, "Failed to fetch plugin configuration from server")
            return Result.success(Unit)
        }

        if (metadataList.isEmpty()) {
            Timber.d("No plugins configured for this server")
            return Result.success(Unit)
        }

        Timber.d("Loading ${metadataList.size} plugin(s)")

        for (metadata in metadataList) {
            runCatching {
                val bytes = pluginDownloader.getOrDownload(metadata).getOrThrow()

                if (!pluginVerifier.verify(bytes, metadata.checksum)) {
                    Timber.e("Plugin '${metadata.id}' failed checksum verification — skipping")
                    pluginDownloader.evict(metadata)
                    return@runCatching
                }

                @Suppress("DEPRECATION")
                val plugin = pluginLoader.load(bytes, metadata)

                plugin.provideKoinModule()?.let { module ->
                    koin.loadModules(listOf(module))
                }

                pluginRegistry.register(plugin)
                Timber.d("Plugin '${metadata.id}' v${metadata.version} loaded successfully")
            }.onFailure { err ->
                Timber.e(err, "Failed to load plugin '${metadata.id}' — skipping")
            }
        }
    }
}
