package org.dhis2.mobile.plugin.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginBundlePluginTest {
    @Test
    fun `defaults name the bundle after the module and its version`() {
        val extension = applyToBareProject()

        assertEquals("my-plugin-1.5.0.zip", extension.bundleFileName.get())
        assertEquals(HostToolchain.MIN_SDK_FLOOR, extension.minApi.get())
        assertTrue(extension.verifyToolchain.get())
        assertTrue(extension.emitDataStoreSnippet.get())
    }

    @Test
    fun `signing defaults to the android debug key`() {
        val signing = applyToBareProject().signing

        assertTrue(
            signing.keystore
                .get()
                .asFile.path
                .endsWith(".android/debug.keystore"),
        )
        assertEquals("androiddebugkey", signing.alias.get())
        assertEquals("android", signing.storePassword.get())
        assertEquals("android", signing.keyPassword.get())
    }

    @Test
    fun `nothing is wired without the android multiplatform library plugin`() {
        // Applying to an unrelated project must be inert rather than fail: everything the plugin
        // wires depends on AGP's Kotlin Multiplatform library plugin being there too.
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(PluginBundlePlugin::class.java)

        assertNull(project.tasks.findByName("buildPluginBundle"))
    }

    private fun applyToBareProject(): PluginBundleExtension {
        val project = ProjectBuilder.builder().withName("my-plugin").build()
        project.version = "1.5.0"
        project.pluginManager.apply(PluginBundlePlugin::class.java)
        return project.extensions.getByType(PluginBundleExtension::class.java)
    }
}
