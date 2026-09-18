package org.dhis2.usescases.datasets.dataSetTable.plugin

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.koin.dsl.koinApplication
import org.mockito.kotlin.mock
import java.io.File

class PluginDataSetInstanceBodyProviderTest {
    private val registry = PluginRegistry()
    private val provider = PluginDataSetInstanceBodyProvider(registry)

    private class FakePlugin : Dhis2Plugin {
        @Composable
        override fun content(context: Dhis2PluginContext) = Unit
    }

    private fun register(vararg dataSetUids: String) =
        registry.register(
            FakePlugin(),
            PluginMetadata(
                id = "org.example",
                version = "1.0.0",
                entryPoint = "org.example.Entry",
                injectionPoints = listOf(InjectionPoint.DATA_SET_INSTANCE_CONTENT),
                slotConfig = mapOf(InjectionPoint.DATA_SET_INSTANCE_CONTENT to config(*dataSetUids)),
            ),
            File("/tmp/org.example"),
            mock<Dhis2PluginContext>(),
            javaClass.classLoader!!,
            koinApplication { },
        )

    private fun config(vararg dataSetUids: String): JsonObject =
        buildJsonObject {
            putJsonArray("dataSetUids") { dataSetUids.forEach { add(it) } }
        }

    private fun parameters(dataSetUid: String) =
        DataSetInstanceParameters(
            dataSetUid = dataSetUid,
            periodId = "202401",
            organisationUnitUid = "orgUnit",
            attributeOptionComboUid = "attrCombo",
            openErrorLocation = false,
        )

    @Test
    fun `no plugin means the host keeps its own body`() {
        assertNull(provider.bodyFor(parameters("dataSetA")))
    }

    @Test
    fun `a data set the plugin does not claim keeps the host body`() {
        register("dataSetB")

        assertNull(provider.bodyFor(parameters("dataSetA")))
    }

    @Test
    fun `a claimed data set is replaced`() {
        register("dataSetA")

        assertNotNull(provider.bodyFor(parameters("dataSetA")))
    }
}
