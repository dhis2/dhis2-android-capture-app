package org.dhis2.mobile.plugin.gradle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class DeterministicZipTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `packing the same content twice produces identical bytes`() {
        val root = temporaryFolder.newFolder("android")
        write(File(root, "classes.dex"), "dex")
        write(File(root, "composeResources/org/values/strings.cvr"), "strings")

        val first = temporaryFolder.newFile("first.zip")
        val second = temporaryFolder.newFile("second.zip")
        DeterministicZip.pack(root, first, prefix = "android/")
        // A wall-clock timestamp in the second build is exactly what breaks reproducibility, so
        // touch the sources in between: only content may influence the output.
        File(root, "classes.dex").setLastModified(0)
        DeterministicZip.pack(root, second, prefix = "android/")

        assertEquals(first.readBytes().toList(), second.readBytes().toList())
    }

    @Test
    fun `packing prefixes every entry and keeps them sorted`() {
        val root = temporaryFolder.newFolder("payload")
        write(File(root, "b.txt"), "b")
        write(File(root, "a.txt"), "a")

        val zip = temporaryFolder.newFile("packed.zip")
        DeterministicZip.pack(root, zip, prefix = "android/")

        assertEquals(listOf("android/a.txt", "android/b.txt"), entryNames(zip))
    }

    @Test
    fun `repacking normalises timestamps and keeps content`() {
        val source = temporaryFolder.newFile("signed.zip")
        ZipOutputStream(source.outputStream()).use { out ->
            listOf("android/classes.dex" to "dex", "META-INF/MANIFEST.MF" to "manifest").forEach {
                out.putNextEntry(ZipEntry(it.first).apply { time = System.currentTimeMillis() })
                out.write(it.second.toByteArray())
                out.closeEntry()
            }
        }

        val first = temporaryFolder.newFile("repacked-first.zip")
        val second = temporaryFolder.newFile("repacked-second.zip")
        DeterministicZip.repack(source, first)
        DeterministicZip.repack(source, second)

        assertEquals(first.readBytes().toList(), second.readBytes().toList())
        assertEquals("dex", ZipFile(first).use { zip -> zip.getInputStream(zip.getEntry("android/classes.dex")).reader().readText() })
        // The source carried a wall-clock time; the repacked copy must not.
        assertNotEquals(sourceTime(source, "android/classes.dex"), sourceTime(first, "android/classes.dex"))
    }

    @Test
    fun `manifest is first and signature files precede the payload`() {
        val ordered =
            DeterministicZip.orderedEntries(
                listOf(
                    "android/classes.dex",
                    "META-INF/ANDROIDD.SF",
                    "META-INF/MANIFEST.MF",
                    "android/composeResources/a.cvr",
                    "META-INF/ANDROIDD.RSA",
                ),
            )

        assertEquals("META-INF/MANIFEST.MF", ordered.first())
        assertEquals(
            listOf("META-INF/ANDROIDD.RSA", "META-INF/ANDROIDD.SF"),
            ordered.subList(1, 3),
        )
        assertTrue(ordered.drop(3).all { it.startsWith("android/") })
    }

    private fun write(
        file: File,
        content: String,
    ) {
        file.parentFile.mkdirs()
        file.writeText(content)
    }

    private fun entryNames(zip: File): List<String> =
        ZipFile(zip).use {
            it
                .entries()
                .asSequence()
                .map { entry -> entry.name }
                .toList()
        }

    private fun sourceTime(
        zip: File,
        entry: String,
    ): Long = ZipFile(zip).use { it.getEntry(entry).time }
}
