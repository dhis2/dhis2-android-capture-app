package org.dhis2.mobile.plugin.gradle

import java.io.File
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Zip writer that produces the same bytes for the same content.
 *
 * Plugin bundles are addressed by SHA-256 in the server dataStore, so a checksum that changes on
 * every rebuild forces an admin edit per iteration. Two things vary between otherwise identical
 * builds: entry order (file-system walk order) and entry timestamps. Both are pinned here — order
 * is sorted, and every entry gets [FIXED_TIME] via `setTimeLocal`, which writes the DOS timestamp
 * directly and is therefore also independent of the build machine's time zone.
 *
 * A JAR signature covers entry *contents*, never their timestamps or order, so [repack] can
 * normalise a signed zip without invalidating it.
 */
internal object DeterministicZip {
    /** Earliest timestamp the DOS format used by zip can represent, plus a month of slack. */
    private val FIXED_TIME: LocalDateTime = LocalDateTime.of(1980, 2, 1, 0, 0, 0)

    private const val MANIFEST_ENTRY = "META-INF/MANIFEST.MF"

    /** Packs every file under [root] into [target], each entry prefixed with [prefix]. */
    fun pack(
        root: File,
        target: File,
        prefix: String,
    ) {
        val relativePaths =
            root
                .walkTopDown()
                .filter { it.isFile }
                .map { it.relativeTo(root).invariantSeparatorsPath }
                .sorted()
                .toList()

        ZipOutputStream(target.outputStream().buffered()).use { out ->
            relativePaths.forEach { path ->
                out.putNextEntry(entry(prefix + path))
                root.resolve(path).inputStream().use { it.copyTo(out) }
                out.closeEntry()
            }
        }
    }

    /**
     * Copies every entry of [source] into [target] byte-for-byte, in [orderedEntries] order and
     * with normalised timestamps. Used after signing, whose output carries wall-clock timestamps.
     */
    fun repack(
        source: File,
        target: File,
    ) {
        ZipFile(source).use { zip ->
            val names =
                zip
                    .entries()
                    .asSequence()
                    .filterNot { it.isDirectory }
                    .map { it.name }
                    .toList()

            ZipOutputStream(target.outputStream().buffered()).use { out ->
                orderedEntries(names).forEach { name ->
                    out.putNextEntry(entry(name))
                    zip.getInputStream(zip.getEntry(name)).use { it.copyTo(out) }
                    out.closeEntry()
                }
            }
        }
    }

    /**
     * Stable entry order for a signed bundle: the manifest first (some readers expect it there),
     * then the rest of `META-INF/` — the signature files — then the payload, each group sorted.
     */
    fun orderedEntries(names: List<String>): List<String> {
        val manifest = names.filter { it == MANIFEST_ENTRY }
        val signatures = names.filter { it != MANIFEST_ENTRY && it.startsWith("META-INF/") }.sorted()
        val payload = names.filterNot { it.startsWith("META-INF/") }.sorted()
        return manifest + signatures + payload
    }

    private fun entry(name: String): ZipEntry =
        ZipEntry(name).apply {
            setTimeLocal(FIXED_TIME)
        }
}
