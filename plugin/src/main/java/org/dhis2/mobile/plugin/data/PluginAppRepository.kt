package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.domain.model.PluginApp
import org.hisp.dhis.android.core.D2
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/** The apps installed on the user's DHIS2 server, reached through the SDK's authenticated client. */
class PluginAppRepository(
    private val d2: D2,
    private val dispatcher: Dispatcher,
) {
    /** The installed app with [key], as `/api/apps` describes it. */
    suspend fun installedApp(key: String): Result<PluginApp> =
        withContext(dispatcher.io) {
            try {
                val response =
                    d2.httpServiceClient().get<String> {
                        url("apps")
                        parameters { attribute("key", key) }
                    }
                val app =
                    (Json.parseToJsonElement(response) as? JsonArray)
                        .orEmpty()
                        .mapNotNull { it as? JsonObject }
                        .firstOrNull { it.string("key") == key }
                        ?: throw NoSuchElementException("App '$key' is not installed on the server")
                Result.success(
                    PluginApp(
                        key = key,
                        name = app.string("name") ?: key,
                        version = app.string("version") ?: error("App '$key' has no version"),
                        appHubId = app.string("app_hub_id"),
                    ),
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Result.failure(error)
            }
        }

    /** A file of an installed app as the server serves it, or null if it cannot be read. */
    suspend fun serverFile(
        key: String,
        path: String,
    ): ByteArray? =
        withContext(dispatcher.io) {
            try {
                d2.httpServiceClient().get<ByteArray> { url("apps/$key/$path") }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Timber.w(error, "Could not read %s of app %s from the server", path, key)
                null
            }
        }

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
}
