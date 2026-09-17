package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.hisp.dhis.android.core.D2
import timber.log.Timber

private const val PLUGIN_NAMESPACE = "dhis2AndroidPlugins"
private const val PLUGIN_CONFIG_KEY = "config"
private const val INJECTION_POINTS_KEY = "injectionPoints"

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
 *       "injectionPoints": ["HOME_ABOVE_PROGRAM_LIST"]
 *     }
 *   ]
 * }
 * ```
 *
 * Returns an empty list (not a failure) if no configuration has been set up yet.
 */
class AppHubPluginRepository(
    private val d2: D2,
    private val dispatcher: Dispatcher,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Pulls the `dhis2AndroidPlugins` namespace into the local dataStore.
     *
     * The SDK's dataStore is a local mirror and nothing else in the app downloads it, so without
     * this a read can only ever return what a previous run cached. Only this namespace is
     * requested — the whole dataStore could be large and is none of the plugin system's business.
     *
     * Offline-first: a failure is reported but not fatal, and [getConfiguredPlugins] still reads
     * whatever was cached. Callers are expected to ignore the result and carry on.
     */
    suspend fun refreshConfiguration(): Result<Unit> =
        withContext(dispatcher.io) {
            runCatching {
                d2
                    .dataStoreModule()
                    .dataStoreDownloader()
                    .byNamespace()
                    .eq(PLUGIN_NAMESPACE)
                    .blockingDownload()
            }
        }

    /**
     * Returns the list of [PluginMetadata] cached locally for this server, or an empty list if no
     * configuration exists. Reads only — call [refreshConfiguration] first to pick up server-side
     * changes.
     */
    suspend fun getConfiguredPlugins(): Result<List<PluginMetadata>> =
        withContext(dispatcher.io) {
            runCatching {
                val entry =
                    d2
                        .dataStoreModule()
                        .dataStore()
                        .byNamespace()
                        .eq(PLUGIN_NAMESPACE)
                        .byKey()
                        .eq(PLUGIN_CONFIG_KEY)
                        .blockingGet()
                        .firstOrNull()

                val configJson = entry?.value()

                if (configJson.isNullOrBlank()) {
                    // Either the admin has not configured any plugins, or the dataStore has not
                    // been synced yet for this user — both are normal, not errors.
                    Timber.d(
                        "No plugin configuration found in server dataStore " +
                            "($PLUGIN_NAMESPACE/$PLUGIN_CONFIG_KEY)",
                    )
                    return@runCatching emptyList()
                }

                val config = json.decodeFromString<PluginConfig>(configJson)
                val plugins = config.plugins.mapNotNull(::decodeEntry)
                Timber.d("Found ${plugins.size} plugin(s) in server configuration")
                plugins
            }
        }

    /**
     * Decodes one entry, or returns null having said why.
     *
     * Per entry rather than for the whole array, so one administrator's typo costs only their own
     * plugin. `LoadPluginsUseCase` already isolates every later step per plugin; parsing was the
     * one place where a single bad entry took every other plugin down with it.
     */
    private fun decodeEntry(entry: JsonObject): PluginMetadata? =
        runCatching {
            json.decodeFromJsonElement(PluginMetadata.serializer(), entry.withKnownInjectionPoints())
        }.getOrElse { error ->
            Timber.w(error, "Skipping unreadable plugin entry %s", entry.pluginId())
            null
        }

    /**
     * The same entry with injection points this app build does not know about removed.
     *
     * Slots are added over time, so a config written for a newer app names slots an older one has
     * never heard of. Dropping the whole entry there would mean a plugin that also renders somewhere
     * this build *does* support disappears, and with it an administrator's working home-screen
     * plugin — for a slot that could not have rendered here anyway.
     *
     * A genuine typo still costs the plugin its slot, and says so in the log. A plugin left with no
     * slot at all simply renders nowhere.
     */
    private fun JsonObject.withKnownInjectionPoints(): JsonObject {
        val declared = this[INJECTION_POINTS_KEY] as? JsonArray ?: return this
        val known = InjectionPoint.entries.mapTo(mutableSetOf()) { it.name }
        val (supported, unsupported) =
            declared.partition { (it as? JsonPrimitive)?.contentOrNull in known }

        if (unsupported.isEmpty()) return this

        Timber.w(
            "Plugin %s declares injection point(s) this app does not support: %s",
            pluginId(),
            unsupported.joinToString { it.toString() },
        )
        return JsonObject(this + (INJECTION_POINTS_KEY to JsonArray(supported)))
    }

    private fun JsonObject.pluginId(): String = runCatching { this["id"]?.jsonPrimitive?.contentOrNull }.getOrNull() ?: "<unnamed>"
}

@kotlinx.serialization.Serializable
private data class PluginConfig(
    /**
     * Left as raw objects so each entry can be decoded, and fail, on its own.
     */
    val plugins: List<JsonObject> = emptyList(),
)
