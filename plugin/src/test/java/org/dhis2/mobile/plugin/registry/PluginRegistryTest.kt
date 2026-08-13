package org.dhis2.mobile.plugin.registry

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PluginRegistryTest {
    private val registry = PluginRegistry()

    /** Minimal stand-in: [Dhis2Plugin.content] is a Composable and never invoked here. */
    private class FakePlugin : Dhis2Plugin {
        @androidx.compose.runtime.Composable
        override fun content(context: Dhis2PluginContext) = Unit
    }

    private fun metadata(
        id: String,
        version: String = "1.0.0",
        slots: List<InjectionPoint> = listOf(InjectionPoint.HOME_ABOVE_PROGRAM_LIST),
    ) = PluginMetadata(
        id = id,
        version = version,
        entryPoint = "$id.Entry",
        injectionPoints = slots,
    )

    private fun root(name: String) = File("/tmp/$name")

    @Test
    fun `starts empty`() {
        assertTrue(registry.plugins.value.isEmpty())
        assertTrue(registry.getPluginsForSlot(InjectionPoint.HOME_ABOVE_PROGRAM_LIST).isEmpty())
    }

    @Test
    fun `registers a plugin with its metadata and resource root`() {
        val plugin = FakePlugin()
        val meta = metadata("org.a")
        val resources = root("a")

        registry.register(plugin, meta, resources)

        val registered = registry.plugins.value.single()
        assertSame(plugin, registered.plugin)
        assertEquals(meta, registered.metadata)
        assertEquals(resources, registered.resourceRoot)
    }

    @Test
    fun `registering different ids accumulates`() {
        registry.register(FakePlugin(), metadata("org.a"), root("a"))
        registry.register(FakePlugin(), metadata("org.b"), root("b"))

        assertEquals(listOf("org.a", "org.b"), registry.plugins.value.map { it.metadata.id })
    }

    @Test
    fun `re-registering the same id replaces instead of duplicating`() {
        // The load pipeline can run more than once per process (logout then re-login);
        // without this the registry would render the same plugin twice.
        registry.register(FakePlugin(), metadata("org.a", version = "1.0.0"), root("v1"))
        registry.register(FakePlugin(), metadata("org.a", version = "2.0.0"), root("v2"))

        val registered = registry.plugins.value.single()
        assertEquals("2.0.0", registered.metadata.version)
        assertEquals(root("v2"), registered.resourceRoot)
    }

    @Test
    fun `re-registering keeps the newest instance`() {
        val old = FakePlugin()
        val new = FakePlugin()
        registry.register(old, metadata("org.a"), root("a"))
        registry.register(new, metadata("org.a"), root("a"))

        assertSame(
            new,
            registry.plugins.value
                .single()
                .plugin,
        )
    }

    @Test
    fun `getPluginsForSlot returns only plugins configured for that slot`() {
        registry.register(FakePlugin(), metadata("org.in-slot"), root("a"))
        registry.register(FakePlugin(), metadata("org.no-slot", slots = emptyList()), root("b"))

        val forSlot = registry.getPluginsForSlot(InjectionPoint.HOME_ABOVE_PROGRAM_LIST)

        assertEquals(listOf("org.in-slot"), forSlot.map { it.metadata.id })
    }

    @Test
    fun `a plugin with no injection points renders nowhere`() {
        registry.register(FakePlugin(), metadata("org.a", slots = emptyList()), root("a"))

        assertEquals(1, registry.plugins.value.size)
        assertTrue(registry.getPluginsForSlot(InjectionPoint.HOME_ABOVE_PROGRAM_LIST).isEmpty())
    }

    @Test
    fun `clear removes everything`() {
        registry.register(FakePlugin(), metadata("org.a"), root("a"))
        registry.register(FakePlugin(), metadata("org.b"), root("b"))

        registry.clear()

        assertTrue(registry.plugins.value.isEmpty())
    }

    @Test
    fun `observers see registrations and clears`() =
        runTest {
            registry.plugins.test {
                assertTrue(awaitItem().isEmpty())

                registry.register(FakePlugin(), metadata("org.a"), root("a"))
                assertEquals(listOf("org.a"), awaitItem().map { it.metadata.id })

                registry.register(FakePlugin(), metadata("org.b"), root("b"))
                assertEquals(listOf("org.a", "org.b"), awaitItem().map { it.metadata.id })

                registry.clear()
                assertTrue(awaitItem().isEmpty())
            }
        }
}
