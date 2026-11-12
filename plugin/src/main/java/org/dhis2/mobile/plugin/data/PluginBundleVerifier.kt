package org.dhis2.mobile.plugin.data

import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/**
 * Checks that a bundle downloaded from the App Hub is the build the DHIS2 server actually runs.
 *
 * It compares the entry page's module script, by SHA-256, with the same file served by the server.
 * The entry page itself cannot be compared, because the server rewrites it to inject its base URL;
 * the script it loads carries a content hash in its name and is served untouched.
 */
object PluginBundleVerifier {
    private val MODULE_SCRIPT = Regex("""<script[^>]*type="module"[^>]*src="\.?/?([^"]+)"""")

    /** The path, relative to the bundle root, of the module script an entry page loads. */
    fun entryScriptPath(html: String): String? = MODULE_SCRIPT.find(html)?.groupValues?.get(1)

    suspend fun verify(
        bundle: ByteArray,
        entry: String,
        serverFile: suspend (path: String) -> ByteArray?,
    ): Result<Unit> {
        val files = readEntries(bundle)
        val html = files[entry]?.decodeToString() ?: return failure("bundle has no entry page '$entry'")
        val script = entryScriptPath(html) ?: return failure("entry page loads no module script")
        val bundled = files[script] ?: return failure("bundle has no '$script'")
        val served = serverFile(script) ?: return failure("server does not serve '$script'")
        return if (sha256(bundled).contentEquals(sha256(served))) {
            Result.success(Unit)
        } else {
            failure("'$script' differs from the build the server runs")
        }
    }

    private fun readEntries(bundle: ByteArray): Map<String, ByteArray> =
        ZipInputStream(ByteArrayInputStream(bundle)).use { zip ->
            generateSequence { zip.nextEntry }
                .filterNot { it.isDirectory }
                .associate { it.name to zip.readBytes() }
        }

    private fun sha256(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)

    private fun failure(reason: String) = Result.failure<Unit>(IllegalStateException("Plugin bundle rejected: $reason"))
}
