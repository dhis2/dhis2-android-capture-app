package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream
import kotlin.coroutines.cancellation.CancellationException

/**
 * Extracts a verified bundle to `{root}/{key}-{version}/`, the directory the WebView serves the
 * plugin from. Once extracted, the plugin renders with no network at all.
 *
 * Extraction goes to a staging directory first and is renamed into place only when complete, so a
 * half-written bundle is never served. Each directory carries a marker recording which app and
 * version it holds: that is how a bundle is found again offline, and how older versions are removed
 * without matching on directory names, which would also catch an app whose key merely starts with
 * this one's.
 */
class PluginBundleInstaller(
    private val root: File,
    private val dispatcher: Dispatcher,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun install(
        key: String,
        version: String,
        entry: String,
        bundle: ByteArray,
    ): Result<RegisteredPlugin> =
        withContext(dispatcher.io) {
            try {
                Result.success(installBlocking(key, version, entry, bundle))
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Result.failure(error)
            }
        }

    /** The installed bundle of exactly this app version, or null. */
    suspend fun installed(
        key: String,
        version: String,
    ): RegisteredPlugin? = withContext(dispatcher.io) { installedBundles().firstOrNull { it.key == key && it.version == version } }

    /** Any installed bundle of this app: what a device that cannot reach the server renders. */
    suspend fun installedAnyVersion(key: String): RegisteredPlugin? =
        withContext(dispatcher.io) {
            installedBundles().firstOrNull {
                it.key ==
                    key
            }
        }

    private fun installedBundles(): List<RegisteredPlugin> =
        root.listFiles().orEmpty().mapNotNull { dir ->
            val marker = File(dir, MARKER).takeIf(File::isFile) ?: return@mapNotNull null
            try {
                val record = json.decodeFromString<BundleRecord>(marker.readText())
                RegisteredPlugin(record.key, record.version, record.entry, dir)
            } catch (error: Exception) {
                Timber.w(error, "Ignoring unreadable plugin bundle at %s", dir)
                null
            }
        }

    private fun installBlocking(
        key: String,
        version: String,
        entry: String,
        bundle: ByteArray,
    ): RegisteredPlugin {
        root.mkdirs()
        val target = File(root, "$key-$version")
        val staging = File(root, ".staging-$key-$version")
        staging.deleteRecursively()
        try {
            extract(bundle, staging)
            check(File(staging, entry).isFile) { "Bundle for '$key' has no entry point '$entry'" }
            File(staging, MARKER).writeText(json.encodeToString(BundleRecord.serializer(), BundleRecord(key, version, entry)))
            target.deleteRecursively()
            check(staging.renameTo(target)) { "Could not move bundle into $target" }
        } finally {
            staging.deleteRecursively()
        }

        installedBundles()
            .filter { it.key == key && it.bundleDir != target }
            .forEach { it.bundleDir.deleteRecursively() }
        Timber.d("Plugin '$key' v$version installed at $target")
        return RegisteredPlugin(key, version, entry, target)
    }

    private fun extract(
        bundle: ByteArray,
        destination: File,
    ) {
        val canonicalDestination = destination.canonicalFile
        ZipInputStream(ByteArrayInputStream(bundle)).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                val out = File(destination, entry.name).canonicalFile
                // Zip slip: an entry like "../../x" would otherwise be written outside the bundle.
                check(out.path.startsWith(canonicalDestination.path + File.separator)) {
                    "Bundle entry escapes its directory: ${entry.name}"
                }
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    out.outputStream().use { zip.copyTo(it) }
                }
            }
        }
    }

    @Serializable
    private data class BundleRecord(
        val key: String,
        val version: String,
        val entry: String,
    )

    private companion object {
        const val MARKER = ".dhis2-plugin.json"
    }
}
