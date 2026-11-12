package org.dhis2.usescases.datasets.dataSetTable.extension

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.extension.host.data.CustomFormRepository
import org.dhis2.mobile.extension.host.domain.LegacyApiScan
import org.dhis2.mobile.extension.host.domain.ResolveCustomForm
import org.dhis2.mobile.extension.host.model.DataSetInstanceRef
import org.dhis2.mobile.extension.host.model.FailureReason
import org.dhis2.mobile.extension.host.renderer.CustomFormRenderer
import org.dhis2.mobile.extension.host.reporting.ExtensionFailureReporter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CustomFormBodyProviderTest {
    private val reporter = ExtensionFailureReporter { _, _ -> }

    private fun provider(html: String?) =
        CustomFormBodyProvider(
            resolveCustomForm =
                ResolveCustomForm(
                    repository =
                        object : CustomFormRepository {
                            override suspend fun customFormHtml(dataSetUid: String) = html

                            override suspend fun serverUsesHttps() = true
                        },
                    renderer = FakeCustomFormRenderer(),
                    rules = listOf(LegacyApiScan()),
                    reporter = reporter,
                ),
            renderer = FakeCustomFormRenderer(),
            reporter = reporter,
        )

    private val parameters = DataSetInstanceParameters("dataSet", "202601", "orgUnit", "aoc", false)

    @Test
    fun `data sets without custom form keep the native tables`() =
        runTest {
            assertNull(provider(null).bodyFor(parameters))
        }

    @Test
    fun `data sets with a supported custom form get a replaced body`() =
        runTest {
            assertNotNull(provider("<input id=\"de-coc-val\"/>").bodyFor(parameters))
        }
}

private class FakeCustomFormRenderer : CustomFormRenderer {
    override fun deviceSupport(): FailureReason? = null

    @Composable
    override fun Render(
        html: String,
        instance: DataSetInstanceRef,
        modifier: Modifier,
        contentPadding: PaddingValues,
        editable: Boolean,
        reloadKey: Int,
        saveValue: suspend (dataElementUid: String, categoryOptionComboUid: String, value: String?) -> String?,
        onFailed: (FailureReason) -> Unit,
    ) = Unit
}
