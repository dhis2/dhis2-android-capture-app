package org.dhis2.mobile.plugin.gradle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassesJarInspectorTest {
    @Test
    fun `a plugin's own classes are allowed`() {
        val forbidden =
            ClassesJarInspector.forbiddenEntries(
                listOf(
                    "org/myorg/plugin/MyPlugin.class",
                    "org/myorg/plugin/generated/resources/Res.class",
                    "META-INF/plugin.kotlin_module",
                ),
            )

        assertTrue(forbidden.isEmpty())
    }

    @Test
    fun `host-owned classes are reported`() {
        val forbidden =
            ClassesJarInspector.forbiddenEntries(
                listOf(
                    "org/myorg/plugin/MyPlugin.class",
                    "org/dhis2/mobile/plugin/sdk/Dhis2Plugin.class",
                    "androidx/compose/material3/CardKt.class",
                    "kotlin/collections/CollectionsKt.class",
                ),
            )

        assertEquals(
            listOf(
                "org/dhis2/mobile/plugin/sdk/Dhis2Plugin.class",
                "androidx/compose/material3/CardKt.class",
                "kotlin/collections/CollectionsKt.class",
            ),
            forbidden,
        )
    }

    @Test
    fun `resource package is read from the generated Res class`() {
        val resourcePackage =
            ClassesJarInspector.resourcePackage(
                listOf(
                    "org/myorg/plugin/MyPlugin.class",
                    "org/myorg/plugin/generated/resources/Res.class",
                    "org/myorg/plugin/generated/resources/Res\$string.class",
                ),
            )

        assertEquals("org.myorg.plugin.generated.resources", resourcePackage)
    }

    @Test
    fun `no Res class means no resource package`() {
        assertNull(ClassesJarInspector.resourcePackage(listOf("org/myorg/plugin/MyPlugin.class")))
    }
}
