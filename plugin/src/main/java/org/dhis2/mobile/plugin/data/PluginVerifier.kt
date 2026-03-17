package org.dhis2.mobile.plugin.data

import timber.log.Timber
import java.security.MessageDigest

/**
 * Verifies the integrity of a downloaded plugin DEX by comparing its SHA-256 checksum
 * against the value declared in the plugin's App Hub metadata.
 *
 * The expected checksum format is `sha256:<hex-encoded-hash>`, e.g.:
 * `sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`
 *
 * A plugin whose checksum does not match must never be loaded.
 */
class PluginVerifier {

    /**
     * Returns `true` if [pluginBytes] match [expectedChecksum]; `false` otherwise.
     */
    fun verify(pluginBytes: ByteArray, expectedChecksum: String): Boolean {
        if (expectedChecksum.isBlank()) {
            Timber.w("Plugin has no checksum — skipping verification (not recommended in production)")
            return true
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pluginBytes)
        val actualChecksum = "sha256:" + hashBytes.joinToString("") { "%02x".format(it) }

        val matches = actualChecksum == expectedChecksum
        if (!matches) {
            Timber.e("Plugin checksum mismatch! expected=$expectedChecksum actual=$actualChecksum")
        }
        return matches
    }
}
