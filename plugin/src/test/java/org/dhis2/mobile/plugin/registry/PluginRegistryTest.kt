package org.dhis2.mobile.plugin.registry

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.plugin.domain.model.FormFieldPluginPlacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PluginRegistryTest {
    private val registry = PluginRegistry()

    private fun plugin(
        key: String,
        version: String = "1",
    ) = RegisteredPlugin(key, version, "plugin.html", File("/bundles/$key-$version"))

    private fun placement(
        formUid: String,
        appKey: String,
    ) = FormFieldPluginPlacement(formUid, "element-$appKey", "/api/apps/$appKey/plugin.html", emptyList())

    @Test
    fun `starts empty`() {
        assertTrue(
            registry.state.value.plugins
                .isEmpty(),
        )
        assertTrue(
            registry.state.value.placements
                .isEmpty(),
        )
    }

    @Test
    fun `re-registering an app replaces the previous version`() {
        registry.register(plugin("a", "1"))
        registry.register(plugin("a", "2"))

        assertEquals(
            "2",
            registry.state.value.plugins
                .getValue("a")
                .version,
        )
    }

    @Test
    fun `a form gets the placements configured for it whose plugin is installed`() {
        registry.register(plugin("matrix-select"))
        registry.setPlacements(
            listOf(
                placement("stage", "matrix-select"),
                placement("other-stage", "matrix-select"),
                placement("stage", "not-installed"),
            ),
        )

        val forForm = registry.state.value.formFieldPlugins("stage")

        assertEquals(listOf("element-matrix-select"), forForm.map { it.placement.elementId })
        assertEquals("matrix-select", forForm.single().plugin.key)
    }

    @Test
    fun `observers see registrations and clears`() =
        runTest {
            registry.state.test {
                assertEquals(0, awaitItem().plugins.size)
                registry.register(plugin("a"))
                assertEquals(1, awaitItem().plugins.size)
                registry.clear()
                assertEquals(0, awaitItem().plugins.size)
            }
        }
}
