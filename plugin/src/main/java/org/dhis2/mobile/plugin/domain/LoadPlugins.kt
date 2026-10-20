package org.dhis2.mobile.plugin.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.plugin.data.AppHubRepository
import org.dhis2.mobile.plugin.data.CaptureConfigRepository
import org.dhis2.mobile.plugin.data.PluginAppRepository
import org.dhis2.mobile.plugin.data.PluginBundleInstaller
import org.dhis2.mobile.plugin.data.PluginBundleVerifier
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/** The entry page of a plugin bundle built by the DHIS2 app platform. */
private const val PLUGIN_ENTRY = "plugin.html"

/**
 * Makes the Capture form field plugins configured on the server renderable on this device.
 *
 * The configuration is the web Capture app's own (`capture/dataEntryForms`). For each app a
 * placement points at, the version installed on the server is downloaded from the App Hub, checked
 * against what the server serves, and extracted. An installed version is never downloaded again, and
 * without a connection the last installed bundle is used, so plugins keep working offline.
 */
class LoadPlugins(
    private val captureConfig: CaptureConfigRepository,
    private val apps: PluginAppRepository,
    private val appHub: AppHubRepository,
    private val installer: PluginBundleInstaller,
    private val registry: PluginRegistry,
) : UseCase<Unit, Unit> {
    override suspend fun invoke(input: Unit): Result<Unit> =
        try {
            load()
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }

    private suspend fun load() {
        captureConfig.refresh().onFailure { Timber.w(it, "Could not refresh the capture configuration — using the cached one") }

        val placements =
            captureConfig.formFieldPlacements().getOrElse {
                Timber.e(it, "Could not read capture/dataEntryForms")
                return
            }
        registry.setPlacements(placements)

        placements
            .mapNotNull { placement ->
                placement.appKey.also {
                    if (it ==
                        null
                    ) {
                        Timber.w("Plugin source %s is not an installed app — skipped", placement.pluginSource)
                    }
                }
            }.distinct()
            .forEach { key -> loadApp(key)?.let(registry::register) }
    }

    private suspend fun loadApp(key: String): RegisteredPlugin? {
        val app =
            apps.installedApp(key).getOrElse { error ->
                if (error is NoSuchElementException) {
                    Timber.w("Plugin app '%s' is not installed on the server — skipped", key)
                    return null
                }
                // Offline: render whichever version was installed last.
                return installer.installedAnyVersion(key)
            }

        installer.installed(app.key, app.version)?.let { return it }

        val bundle =
            appHub
                .downloadUrl(app)
                .mapCatching { url -> appHub.download(url).getOrThrow() }
                .getOrElse {
                    Timber.e(it, "Could not download %s %s from the App Hub", app.name, app.version)
                    return null
                }

        PluginBundleVerifier.verify(bundle, PLUGIN_ENTRY) { path -> apps.serverFile(key, path) }.onFailure {
            Timber.e(it, "Not installing %s %s", app.name, app.version)
            return null
        }

        return installer.install(key, app.version, PLUGIN_ENTRY, bundle).getOrElse {
            Timber.e(it, "Could not install %s %s", app.name, app.version)
            null
        }
    }
}
