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
    fun `the DHIS2 SDK, the design system, Koin, coroutines and lifecycle are host-owned too`() {
        // Each of these is in PluginConventions.HOST_PROVIDED. A plugin that bundles one used to
        // pass this check and then ClassCastException on a device.
        val forbidden =
            ClassesJarInspector.forbiddenEntries(
                listOf(
                    "org/myorg/plugin/MyPlugin.class",
                    "org/hisp/dhis/android/core/D2.class",
                    "org/hisp/dhis/mobile/ui/designsystem/theme/SurfaceColor.class",
                    "org/koin/core/module/Module.class",
                    "kotlinx/coroutines/Dispatchers.class",
                    "androidx/lifecycle/ViewModel.class",
                ),
            )

        assertEquals(
            listOf(
                "org/hisp/dhis/android/core/D2.class",
                "org/hisp/dhis/mobile/ui/designsystem/theme/SurfaceColor.class",
                "org/koin/core/module/Module.class",
                "kotlinx/coroutines/Dispatchers.class",
                "androidx/lifecycle/ViewModel.class",
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
