package org.dhis2.mobile.plugin.data

import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import java.util.jar.JarFile

/**
 * Verifies the integrity and authorship of a downloaded plugin bundle.
 *
 * Two checks run, both mandatory:
 *
 * 1. **SHA-256** — the bundle's bytes must match [PluginMetadata.checksum]
 *    (`sha256:<hex>`). This protects against in-transit corruption and trivial tampering.
 *    A blank checksum is accepted with a warning (useful during local iteration; not
 *    recommended in production).
 *
 * 2. **JAR signature** — the bundle is a zip signed with `jarsigner`. Every entry
 *    inside must be covered by a valid signature in `META-INF/`. We enforce this by
 *    opening the zip with `JarFile(file, verify = true)` and reading every entry
 *    end-to-end, which forces the runtime to compute and verify signatures.
 */
class PluginVerifier {

    /** SHA-256 check. Returns `true` if [bytes] match [expectedChecksum]. */
    fun verify(bytes: ByteArray, expectedChecksum: String): Boolean {
        if (expectedChecksum.isBlank()) {
            Timber.w("Plugin has no checksum — skipping verification (not recommended in production)")
            return true
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        val actualChecksum = "sha256:" + hashBytes.joinToString("") { "%02x".format(it) }

        val matches = actualChecksum == expectedChecksum
        if (!matches) {
            Timber.e("Plugin checksum mismatch! expected=$expectedChecksum actual=$actualChecksum")
        }
        return matches
    }

    /**
     * Verifies that [bundle] is a properly signed JAR. Every non-signature entry must be
     * covered by a valid signature block in `META-INF/`. Returns [Result.success] on OK,
     * or [Result.failure] with an explanatory exception if any entry is unsigned or the
     * signature does not match the content.
     *
     * This does NOT pin against any specific certificate — any valid signature passes.
     * Certificate allow-lists can be layered on top later.
     */
    fun verifySignature(bundle: File): Result<Unit> = runCatching {
        JarFile(bundle, /* verify = */ true).use { jar ->
            val entries = jar.entries().toList()
            check(entries.any { it.name.startsWith("META-INF/") && it.name.endsWith(".RSA") || it.name.endsWith(".DSA") || it.name.endsWith(".EC") }) {
                "Plugin bundle is not signed (no META-INF/*.RSA|DSA|EC entry)"
            }
            for (entry in entries) {
                if (entry.isDirectory || entry.name.startsWith("META-INF/")) continue
                // Reading the full entry forces JarFile to compute and verify signatures.
                jar.getInputStream(entry).use { it.readBytes() }
                val certs = entry.certificates
                check(!certs.isNullOrEmpty()) {
                    "Unsigned entry in plugin bundle: ${entry.name}"
                }
            }
        }
    }.onFailure { err ->
        Timber.e(err, "Plugin bundle signature verification failed")
    }
}
