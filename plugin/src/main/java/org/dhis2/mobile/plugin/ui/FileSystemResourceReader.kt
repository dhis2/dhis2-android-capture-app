package org.dhis2.mobile.plugin.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.ResourceReader
import java.io.File
import java.io.RandomAccessFile

/**
 * A [ResourceReader] that reads compose resource bytes from a filesystem [root].
 *
 * CMP's default Android reader opens resources via `Context.assets.open(path)`, which
 * only sees APK-packaged assets — a plugin's DEX-distributed resources aren't there.
 * This reader bypasses AssetManager entirely and reads straight from the plugin's
 * extracted bundle directory (typically `{filesDir}/plugins/{id}-{version}/android`).
 *
 * Path layout expected under [root]:
 *   `composeResources/{package}/values[-locale]/strings.commonMain.cvr`
 *   `composeResources/{package}/drawable/foo.xml`
 */
@OptIn(ExperimentalResourceApi::class)
class FileSystemResourceReader(
    private val root: File,
) : ResourceReader {
    override suspend fun read(path: String): ByteArray =
        withContext(Dispatchers.IO) {
            resolve(path).readBytes()
        }

    override suspend fun readPart(
        path: String,
        offset: Long,
        size: Long,
    ): ByteArray =
        withContext(Dispatchers.IO) {
            RandomAccessFile(resolve(path), "r").use { raf ->
                raf.seek(offset)
                val buf = ByteArray(size.toInt())
                raf.readFully(buf)
                buf
            }
        }

    override fun getUri(path: String): String = resolve(path).toURI().toString()

    /**
     * Resolves [path] under [root], refusing anything that escapes it.
     *
     * The path comes from the plugin's own generated resource accessors, but this reader is handed
     * to plugin code through a CompositionLocal and nothing stops a plugin calling it directly with
     * a path of its choosing — `../../../databases/dhis.db` would otherwise read the app's database.
     */
    private fun resolve(path: String): File {
        val target = File(root, path)
        val canonicalRoot = root.canonicalPath + File.separator
        require(target.canonicalPath.startsWith(canonicalRoot)) {
            "Resource path '$path' resolves outside the plugin's resource directory"
        }
        return target
    }
}
