package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.domain.model.PluginApp
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.api.RequestBuilder
import java.net.URLEncoder
import kotlin.coroutines.cancellation.CancellationException

/**
 * Finds and downloads, from the DHIS2 App Hub, the bundle of an app installed on the server.
 *
 * Requests go through the SDK's client as external requests without credentials: the user's DHIS2
 * credentials are never sent to the App Hub.
 */
class AppHubRepository(
    private val d2: D2,
    private val dispatcher: Dispatcher,
) {
    /**
     * The download URL of the App Hub release matching [app]'s installed version.
     *
     * The server reports `app_hub_id` only when the plugin author declared one, which many do not;
     * without it the App Hub is searched by the installed app's exact name. Either way the bundle is
     * then checked against what the server serves, so a wrong match cannot be installed.
     */
    suspend fun downloadUrl(app: PluginApp): Result<String> =
        io {
            val candidates =
                app.appHubId?.let(::listOf) ?: AppHub.appIdsNamed(get("${AppHub.API}/apps?query=${encode(app.name)}"), app.name)
            candidates.firstNotNullOfOrNull { id -> AppHub.downloadUrlFor(get("${AppHub.API}/apps/$id/versions"), app.version) }
                ?: throw NoSuchElementException("${app.name} ${app.version} is not published on the App Hub")
        }

    suspend fun download(url: String): Result<ByteArray> = io { d2.httpServiceClient().get<ByteArray> { external(url) } }

    private suspend fun get(url: String): String = d2.httpServiceClient().get<String> { external(url) }

    private fun RequestBuilder.external(url: String) {
        absoluteUrl(url, true)
        excludeCredentials()
    }

    private fun encode(value: String) = URLEncoder.encode(value, Charsets.UTF_8.name())

    private suspend fun <T> io(block: suspend () -> T): Result<T> =
        withContext(dispatcher.io) {
            try {
                Result.success(block())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
}
