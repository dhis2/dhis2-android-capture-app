package org.dhis2.mobile.plugin.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GetPluginSlotContentTest {
    private val registry = PluginRegistry()
    private val useCase = GetPluginSlotContent(registry)

    private class FakePlugin : Dhis2Plugin {
        @androidx.compose.runtime.Composable
        override fun content(context: Dhis2PluginContext) = Unit
    }

    private fun register(
        id: String,
        slots: List<InjectionPoint>,
    ) = registry.register(
        FakePlugin(),
        PluginMetadata(
            id = id,
            version = "1.0.0",
            entryPoint = "$id.Entry",
            injectionPoints = slots,
        ),
        File("/tmp/$id"),
    )

    @Test
    fun `returns an empty list when nothing is registered`() =
        runTest {
            val result = useCase(InjectionPoint.HOME_ABOVE_PROGRAM_LIST)

            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isEmpty())
        }

    @Test
    fun `returns the plugins registered for the requested slot`() =
        runTest {
            register("org.a", listOf(InjectionPoint.HOME_ABOVE_PROGRAM_LIST))
            register("org.b", emptyList())

            val ids = useCase(InjectionPoint.HOME_ABOVE_PROGRAM_LIST).getOrThrow().map { it.metadata.id }

            assertEquals(listOf("org.a"), ids)
        }

    @Test
    fun `carries the resource root through so the slot can resolve plugin resources`() =
        runTest {
            register("org.a", listOf(InjectionPoint.HOME_ABOVE_PROGRAM_LIST))

            val registered = useCase(InjectionPoint.HOME_ABOVE_PROGRAM_LIST).getOrThrow().single()

            assertEquals(File("/tmp/org.a"), registered.resourceRoot)
        }

    @Test
    fun `an empty slot is a success not a failure`() =
        runTest {
            register("org.a", emptyList())

            assertTrue(useCase(InjectionPoint.HOME_ABOVE_PROGRAM_LIST).isSuccess)
        }
}
