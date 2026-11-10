package org.dhis2.usescases.datasets.dataSetTable.extension

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.extension.host.data.CustomFormRepository
import org.dhis2.mobile.extension.host.domain.LegacyApiScan
import org.dhis2.mobile.extension.host.domain.ResolveCustomForm
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
                    renderer = PlaceholderCustomFormRenderer(),
                    rules = listOf(LegacyApiScan()),
                    reporter = reporter,
                ),
            renderer = PlaceholderCustomFormRenderer(),
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
