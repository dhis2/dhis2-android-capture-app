package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.hisp.dhis.android.core.D2
import timber.log.Timber

private const val PLUGIN_NAMESPACE = "dhis2AndroidPlugins"
private const val PLUGIN_CONFIG_KEY = "config"

// TODO: remove — hardcoded config for local testing while the dataStore entry is not yet set up.
//TODO: we need to download the dataStore to use it. Ideally the Android SDK is providing this info.
private const val FALLBACK_CONFIG_JSON = """
{
    "plugins": [
        {
            "id": "org.dhis2.myplugin",
            "version": "1.3.0",
            "checksum": "sha256:bd03a2cbac9373ee68d84d7dce1877fee86702b6bd2a5240930a7cc20cf5c732",
            "entryPoint": "org.dhis2.pluginimplementationtest.MyPlugin",
            "downloadUrl": "http://10.0.2.2:8080/org.dhis2.myplugin-1.3.0.zip",
            "injectionPoints": ["HOME_ABOVE_PROGRAM_LIST"],
            "allowedDataSetUids": [],
            "allowedProgramUids": ["IpHINAT79UW"]
        }
    ]
}
"""

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

                val configJson = entry?.value() ?: run {
                    Timber.w("No plugin configuration in server dataStore — using hardcoded fallback")
                    FALLBACK_CONFIG_JSON
                }
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
