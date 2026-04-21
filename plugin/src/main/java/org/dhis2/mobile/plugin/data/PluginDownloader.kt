package org.dhis2.mobile.plugin.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import timber.log.Timber
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads signed plugin bundles (zip) from the App Hub and caches them on disk.
 *
 * Cached files are stored at `{filesDir}/plugins/{pluginId}-{version}.zip` and reused
 * across app restarts to avoid redundant downloads. A new version always overwrites the cache.
 *
 * Note: The download is performed by the host app (not the plugin) so it is not subject to the
 * plugin's network restriction (which only allows communication with the DHIS2 server).
 */
class PluginDownloader(private val context: Context) {

    private val pluginDir: File
        get() = File(context.filesDir, "plugins").also { it.mkdirs() }

    /**
     * Returns a [File] pointing at the on-disk cached zip for [metadata]. If the bundle has not
     * been downloaded yet it is fetched from [PluginMetadata.downloadUrl] first.
     *
     * @return [Result.success] with the cached file, or [Result.failure] on any I/O error.
     */
    suspend fun getOrDownload(metadata: PluginMetadata): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cachedFile = cacheFile(metadata)
                if (cachedFile.exists()) {
                    Timber.d("Plugin '${metadata.id}' v${metadata.version} loaded from cache")
                    return@runCatching cachedFile
                }

                Timber.d("Downloading plugin '${metadata.id}' v${metadata.version} from ${metadata.downloadUrl}")
                val bytes = download(metadata.downloadUrl)

                cachedFile.writeBytes(bytes)
                Timber.d("Plugin cached to ${cachedFile.absolutePath}")
                cachedFile
            }
        }

    /** Removes the cached zip file for [metadata], forcing a re-download on next call. */
    fun evict(metadata: PluginMetadata) {
        cacheFile(metadata).delete()
    }

    private fun download(urlString: String): ByteArray {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        try {
            connection.connect()
            check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "HTTP ${connection.responseCode} when downloading plugin from $urlString"
            }
            return connection.inputStream.readBytes()
        } finally {
            connection.disconnect()
        }
    }

    private fun cacheFile(metadata: PluginMetadata) =
        File(pluginDir, "${metadata.id}-${metadata.version}.zip")

    private companion object {
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 30_000
    }
}
