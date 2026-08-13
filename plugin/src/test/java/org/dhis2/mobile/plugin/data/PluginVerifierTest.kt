package org.dhis2.mobile.plugin.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * The integrity half of the plugin security model.
 *
 * Checksums are computed for real here rather than mocked — a hash test that stubs the hash
 * proves nothing. Signature tests build real zips on disk, because the production code delegates
 * to `JarFile(file, verify = true)` and only real archive bytes exercise that path.
 */
class PluginVerifierTest {
    @get:Rule
    val temp = TemporaryFolder()

    private val verifier = PluginVerifier()

    private fun sha256Of(bytes: ByteArray): String =
        "sha256:" +
            MessageDigest
                .getInstance("SHA-256")
                .digest(bytes)
                .joinToString("") { "%02x".format(it) }

    /** An ordinary zip with no `META-INF/` signature block. */
    private fun unsignedZip(name: String = "unsigned.zip"): File {
        val file = temp.newFile(name)
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("android/classes.dex"))
            zip.write("not really dex".toByteArray())
            zip.closeEntry()
        }
        return file
    }

    // ── SHA-256 ───────────────────────────────────────────────────────────────

    @Test
    fun `accepts bytes matching the expected checksum`() {
        val bytes = "plugin bundle contents".toByteArray()

        assertTrue(verifier.verify(bytes, sha256Of(bytes)))
    }

    @Test
    fun `rejects bytes that do not match`() {
        val bytes = "plugin bundle contents".toByteArray()
        val otherChecksum = sha256Of("different contents".toByteArray())

        assertFalse(verifier.verify(bytes, otherChecksum))
    }

    @Test
    fun `rejects a single flipped byte`() {
        val bytes = "plugin bundle contents".toByteArray()
        val checksum = sha256Of(bytes)
        val tampered = bytes.copyOf().also { it[0] = (it[0] + 1).toByte() }

        assertFalse(verifier.verify(tampered, checksum))
    }

    @Test
    fun `a blank checksum skips the check`() {
        // Documented escape hatch for local iteration: jarsigner embeds a timestamp, so the
        // bundle hash changes on every rebuild. Signature verification still applies.
        assertTrue(verifier.verify("anything at all".toByteArray(), ""))
        assertTrue(verifier.verify("anything at all".toByteArray(), "   "))
    }

    @Test
    fun `a checksum without the sha256 prefix does not match`() {
        // The config format is documented as "sha256:<hex>"; a bare hex string is a
        // misconfiguration and must fail rather than appear to pass.
        val bytes = "plugin bundle contents".toByteArray()
        val bare = sha256Of(bytes).removePrefix("sha256:")

        assertFalse(verifier.verify(bytes, bare))
    }

    @Test
    fun `checksum comparison is case sensitive on the hex digits`() {
        val bytes = "plugin bundle contents".toByteArray()

        assertFalse(verifier.verify(bytes, sha256Of(bytes).uppercase()))
    }

    @Test
    fun `hashes empty content consistently`() {
        val empty = ByteArray(0)

        assertTrue(verifier.verify(empty, sha256Of(empty)))
        assertFalse(verifier.verify(empty, sha256Of("x".toByteArray())))
    }

    // ── JAR signature ─────────────────────────────────────────────────────────

    @Test
    fun `an unsigned zip is rejected`() {
        val result = verifier.verifySignature(unsignedZip())

        assertTrue(result.isFailure)
        assertTrue(
            "expected a 'not signed' explanation, got: ${result.exceptionOrNull()?.message}",
            result.exceptionOrNull()?.message?.contains("not signed") == true,
        )
    }

    @Test
    fun `signature failures are returned rather than thrown`() {
        // Same reasoning as scope violations: the load pipeline isolates failures per plugin,
        // so this must be an inert failed Result.
        val result = verifier.verifySignature(unsignedZip())

        assertTrue(result.isFailure)
    }

    @Test
    fun `a file that is not a zip is rejected`() {
        val notAZip = temp.newFile("garbage.zip").apply { writeText("this is not an archive") }

        assertTrue(verifier.verifySignature(notAZip).isFailure)
    }

    @Test
    fun `a missing file is rejected`() {
        val missing = File(temp.root, "does-not-exist.zip")

        assertTrue(verifier.verifySignature(missing).isFailure)
    }

    @Test
    fun `an empty zip is rejected for having no signature block`() {
        val emptyZip = temp.newFile("empty.zip")
        ZipOutputStream(emptyZip.outputStream()).use { }

        assertTrue(verifier.verifySignature(emptyZip).isFailure)
    }
}
