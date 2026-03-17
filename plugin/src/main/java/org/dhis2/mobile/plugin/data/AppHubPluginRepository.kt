package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.hisp.dhis.android.core.D2
import timber.log.Timber

private const val PLUGIN_NAMESPACE = "dhis2AndroidPlugins"
private const val PLUGIN_CONFIG_KEY = "config"

/**
 * Fetches the list of plugins configured for this DHIS2 server instance.
 *
 * The server admin configures plugins by writing a JSON object to the DHIS2 server-side
 * dataStore at namespace `dhis2AndroidPlugins` / key `config`. The JSON structure is:
 *
 * ```json
 * {
 *   "plugins": [
 *     {
 *       "id": "org.myorg.my-plugin",
 *       "version": "1.0.0",
 *       "entryPoint": "org.myorg.plugin.MyPlugin",
 *       "downloadUrl": "https://apps.dhis2.org/api/apps/my-plugin/1.0.0/plugin.dex",
 *       "checksum": "sha256:abc123...",
 *       "allowedProgramUids": ["UID1"],
 *       "allowedDataSetUids": [],
 *       "injectionPoints": ["HOME_ABOVE_PROGRAM_LIST"]
 *     }
 *   ]
 * }
 * ```
 *
 * Returns an empty list (not a failure) if no configuration has been set up yet.
 */
class AppHubPluginRepository(private val d2: D2) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Returns the list of [PluginMetadata] configured on the server, or an empty list
     * if no configuration exists.
     */
    suspend fun getConfiguredPlugins(): Result<List<PluginMetadata>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val entry = d2.dataStoreModule()
                    .dataStore()
                    .byNamespace().eq(PLUGIN_NAMESPACE)
                    .byKey().eq(PLUGIN_CONFIG_KEY)
                    .blockingGet()
                    .firstOrNull()

                if (entry == null) {
                    Timber.d("No plugin configuration found in server dataStore")
                    return@runCatching emptyList()
                }

                val configJson = entry.value() ?: return@runCatching emptyList()
                val config = json.decodeFromString<PluginConfig>(configJson)
                Timber.d("Found ${config.plugins.size} plugin(s) in server configuration")
                config.plugins
            }
        }
}

@kotlinx.serialization.Serializable
private data class PluginConfig(
    val plugins: List<PluginMetadata> = emptyList(),
)
