package org.dhis2.mobile.plugin.registry

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.plugin.domain.model.FormFieldPluginPlacement
import org.dhis2.mobile.plugin.domain.model.FormLayout
import org.dhis2.mobile.plugin.domain.model.FormLayoutElement
import org.dhis2.mobile.plugin.domain.model.FormLayoutSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PluginRegistryTest {
    private val registry = PluginRegistry()

    private fun plugin(
        key: String,
        version: String = "1",
    ) = RegisteredPlugin(key, version, "plugin.html", File("/bundles/$key-$version"))

    private fun pluginElement(
        formUid: String,
        appKey: String,
    ) = FormLayoutElement.Plugin(
        FormFieldPluginPlacement(formUid, "element-$appKey", "/api/apps/$appKey/plugin.html", emptyList()),
    )

    private fun layout(
        formUid: String,
        vararg elements: FormLayoutElement,
    ) = FormLayout(formUid, listOf(FormLayoutSection("section", null, elements.toList())))

    @Test
    fun `starts empty`() {
        assertTrue(
            registry.state.value.plugins
                .isEmpty(),
        )
        assertTrue(
            registry.state.value.layouts
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
    fun `a form gets the plugins configured for it whose bundle is installed`() {
        registry.register(plugin("matrix-select"))
        registry.setLayouts(
            listOf(
                layout("stage", pluginElement("stage", "matrix-select"), pluginElement("stage", "not-installed")),
                layout("other-stage", pluginElement("other-stage", "matrix-select")),
            ),
        )

        val forForm = registry.state.value.formFieldPlugins("stage")

        assertEquals(listOf("element-matrix-select"), forForm.map { it.placement.elementId })
        assertEquals("matrix-select", forForm.single().plugin.key)
    }

    @Test
    fun `a form's resolved layout keeps its fields and drops plugins that are not installed`() {
        registry.register(plugin("matrix-select"))
        registry.setLayouts(
            listOf(
                layout(
                    "stage",
                    FormLayoutElement.Field("de1"),
                    pluginElement("stage", "matrix-select"),
                    pluginElement("stage", "not-installed"),
                ),
            ),
        )

        val resolved = registry.state.value.resolvedLayout("stage")!!

        val elements =
            resolved.layout.sections
                .single()
                .elements
        assertEquals(2, elements.size)
        assertEquals(FormLayoutElement.Field("de1"), elements[0])
        assertEquals(setOf("element-matrix-select"), resolved.plugins.keys)
    }

    @Test
    fun `a form without a configured layout has none`() {
        assertNull(registry.state.value.resolvedLayout("stage"))
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
