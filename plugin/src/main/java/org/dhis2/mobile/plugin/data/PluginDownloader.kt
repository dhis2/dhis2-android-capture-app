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
 *
 * A response is only cached if it is an `HTTP 200` **and** begins with the zip magic bytes, so a
 * URL that answers with HTML (a login page, an SPA fallback, a 404 page) fails here with a clear
 * message rather than being cached and rejected later as a checksum mismatch.
 */
class PluginDownloader(
    private val context: Context,
) {
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
        // Redirects are resolved manually rather than by HttpURLConnection, for two reasons:
        // every hop gets logged (a bundle URL that redirects is usually a port serving something
        // else, e.g. a DHIS2 login page, and a silently-followed redirect caches that response as
        // the bundle), and cross-protocol hops (http -> https) work, which the built-in following
        // refuses to do. App Hub URLs legitimately redirect to a CDN, so hops are allowed.
        var currentUrl = urlString
        repeat(MAX_REDIRECTS + 1) {
            val connection =
                (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    instanceFollowRedirects = false
                }
            try {
                connection.connect()
                val code = connection.responseCode

                if (code in REDIRECT_CODES) {
                    val location = connection.getHeaderField("Location")
                    checkNotNull(location) {
                        "HTTP $code from $currentUrl with no Location header"
                    }
                    // Resolve against the current URL so relative Location headers work.
                    val target = URL(URL(currentUrl), location).toString()
                    Timber.w("Plugin download redirected: $currentUrl -> $target")
                    currentUrl = target
                    return@repeat
                }

                check(code == HttpURLConnection.HTTP_OK) {
                    "HTTP $code when downloading plugin from $currentUrl"
                }

                val bytes = connection.inputStream.readBytes()
                check(bytes.hasZipMagic()) {
                    "Response from $currentUrl is not a zip bundle: ${bytes.size} bytes, " +
                        "content-type=${connection.contentType}. Is a plugin bundle served there?"
                }
                return bytes
            } finally {
                connection.disconnect()
            }
        }
        error("Too many redirects (> $MAX_REDIRECTS) downloading plugin from $urlString")
    }

    /**
     * True if these bytes begin with the zip local-file-header signature. Catches the common
     * misconfiguration where the download URL answers with HTML (a login page, a 404 page)
     * under a 200 status, which would otherwise be cached and fail as a checksum mismatch.
     */
    private fun ByteArray.hasZipMagic(): Boolean = size >= ZIP_MAGIC.size && ZIP_MAGIC.indices.all { this[it] == ZIP_MAGIC[it] }

    private fun cacheFile(metadata: PluginMetadata) = File(pluginDir, "${metadata.id}-${metadata.version}.zip")

    private companion object {
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 30_000
        const val MAX_REDIRECTS = 5

        /** Codes treated as a redirect, including 307/308 which `HttpURLConnection` never follows. */
        val REDIRECT_CODES =
            setOf(
                HttpURLConnection.HTTP_MOVED_PERM,
                HttpURLConnection.HTTP_MOVED_TEMP,
                HttpURLConnection.HTTP_SEE_OTHER,
                307,
                308,
            )

        /** `PK\x03\x04` — the local-file-header signature that starts every non-empty zip. */
        val ZIP_MAGIC = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
    }
}
