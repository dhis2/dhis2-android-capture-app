package org.dhis2.mobile.plugin.gradle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reads its own compiled class file, so the fixture is a real class produced by the same compiler
 * that will compile plugins — a hand-rolled byte array would only prove the parser agrees with
 * itself.
 */
class ConstantPoolReaderTest {
    private fun ownClassBytes(): ByteArray =
        checkNotNull(
            javaClass.getResourceAsStream("/org/dhis2/mobile/plugin/gradle/ConstantPoolReaderTest.class"),
        ) { "Could not find this test's own class file on the test classpath" }.use { it.readBytes() }

    @Test
    fun `finds the types a class references`() {
        val referenced = ConstantPoolReader.classNames(ownClassBytes())

        // Referenced right here in this file, so they must appear.
        assertTrue(
            "expected ConstantPoolReader among $referenced",
            referenced.contains("org/dhis2/mobile/plugin/gradle/ConstantPoolReader"),
        )
        assertTrue(referenced.contains("org/dhis2/mobile/plugin/gradle/ConstantPoolReaderTest"))
    }

    @Test
    fun `returns empty for something that is not a class file`() {
        assertEquals(emptySet<String>(), ConstantPoolReader.classNames(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)))
        assertEquals(emptySet<String>(), ConstantPoolReader.classNames(ByteArray(0)))
    }

    @Test
    fun `flags references the host will refuse`() {
        val denied =
            ClassesJarInspector.deniedReferencesIn(
                listOf(
                    "org/hisp/dhis/android/core/D2Manager",
                    "org/koin/core/context/GlobalContext",
                    "org/dhis2/usescases/main/MainViewModel",
                    "org/hisp/dhis/android/core/datastore/DataStoreModule",
                ),
            )

        assertEquals(
            listOf(
                "org/dhis2/usescases/main/MainViewModel",
                "org/hisp/dhis/android/core/D2Manager",
                "org/hisp/dhis/android/core/datastore/DataStoreModule",
                "org/koin/core/context/GlobalContext",
            ),
            denied,
        )
    }

    @Test
    fun `exempts the plugin's own classes even when they sit under a denied prefix`() {
        // The official sample plugin is `org.dhis2.pluginimplementationtest`, which matches the
        // `org/dhis2/` rule aimed at the host's classes. A plugin referencing itself is not reaching
        // for anything the host owns.
        val own =
            setOf(
                "org/dhis2/pluginimplementationtest/MyPlugin",
                "org/dhis2/pluginimplementationtest/ProgramSummary",
            )

        val denied =
            ClassesJarInspector.deniedReferencesIn(
                listOf(
                    "org/dhis2/pluginimplementationtest/MyPlugin",
                    "org/dhis2/pluginimplementationtest/ProgramSummary",
                    // Synthetic lambda classes are referenced with a `$` suffix off the outer class.
                    "org/dhis2/pluginimplementationtest/MyPlugin\$content\$state\$2\$1",
                    // …but a genuine host class in the same shape is still caught.
                    "org/dhis2/usescases/main/MainViewModel",
                ),
                own,
            )

        assertEquals(listOf("org/dhis2/usescases/main/MainViewModel"), denied)
    }

    @Test
    fun `leaves the plugin API and the scoped SDK alone`() {
        val denied =
            ClassesJarInspector.deniedReferencesIn(
                listOf(
                    "org/dhis2/mobile/plugin/sdk/Dhis2Plugin",
                    "org/dhis2/mobile/plugin/sdk/Dhis2PluginContext",
                    "org/hisp/dhis/android/core/scopedaccess/ScopedD2",
                    "org/hisp/dhis/android/core/event/EventCollectionRepository",
                    "androidx/compose/runtime/Composer",
                ),
            )

        assertTrue("nothing should be flagged, got $denied", denied.isEmpty())
    }
}
