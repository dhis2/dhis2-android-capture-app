package org.dhis2.mobile.extension.host

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.extension.host.data.CustomFormRepository
import org.dhis2.mobile.extension.host.domain.LegacyApiScan
import org.dhis2.mobile.extension.host.domain.ResolveCustomForm
import org.dhis2.mobile.extension.host.model.DataSetInstanceRef
import org.dhis2.mobile.extension.host.model.FailureReason
import org.dhis2.mobile.extension.host.renderer.CustomFormRenderer
import org.dhis2.mobile.extension.host.reporting.ExtensionFailureReporter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ResolveCustomFormTest {
    private val dataSetUid = "BfMAe6Itzgt"
    private val reported = mutableListOf<FailureReason>()

    private fun resolver(
        html: () -> String?,
        deviceSupport: FailureReason? = null,
        https: Boolean = true,
        allowHttp: Boolean = false,
    ) = ResolveCustomForm(
        repository =
            object : CustomFormRepository {
                override suspend fun customFormHtml(dataSetUid: String) = html()

                override suspend fun serverUsesHttps() = https
            },
        renderer = FakeRenderer(deviceSupport),
        rules = listOf(LegacyApiScan()),
        reporter = ExtensionFailureReporter { _, reason -> reported += reason },
        allowHttp = allowHttp,
    )

    @Test
    fun `a data set without custom form is native and nothing is reported`() =
        runTest {
            assertNull(resolver({ null })(dataSetUid))
            assertEquals(emptyList(), reported)
        }

    @Test
    fun `a supported custom form is shown and nothing is reported`() =
        runTest {
            val html = """<input id="a-b-val"/><script>dhis2.util.on(dhis2.de.event.formReady, f)</script>"""
            val resolved = resolver({ html })(dataSetUid)
            assertNotNull(resolved)
            assertEquals(html, resolved.html)
            assertEquals(emptyList(), reported)
        }

    @Test
    fun `an empty form fails`() =
        runTest {
            assertNull(resolver({ "  " })(dataSetUid))
            assertEquals(listOf(FailureReason.EMPTY_CONTENT), reported)
        }

    @Test
    fun `a form from an http server fails`() =
        runTest {
            assertNull(resolver({ "<p/>" }, https = false)(dataSetUid))
            assertEquals(listOf(FailureReason.INSECURE_SERVER), reported)
        }

    @Test
    fun `a debug build shows a form from an http server`() =
        runTest {
            assertNotNull(resolver({ "<p/>" }, https = false, allowHttp = true)(dataSetUid))
            assertEquals(emptyList(), reported)
        }

    @Test
    fun `an old WebView fails the form`() =
        runTest {
            assertNull(resolver({ "<p/>" }, FailureReason.WEBVIEW_NOT_SUPPORTED)(dataSetUid))
            assertEquals(listOf(FailureReason.WEBVIEW_NOT_SUPPORTED), reported)
        }

    @Test
    fun `a legacy call the shim lacks fails the form`() =
        runTest {
            assertNull(resolver({ "<script>dhis2.de.validate()</script>" })(dataSetUid))
            assertEquals(listOf(FailureReason.UNSUPPORTED_FEATURE), reported)
        }

    @Test
    fun `an unreadable form fails and is reported on every open`() =
        runTest {
            val resolve = resolver({ error("db") })
            assertNull(resolve(dataSetUid))
            assertNull(resolve(dataSetUid))
            assertEquals(List(2) { FailureReason.CONTENT_UNREADABLE }, reported)
        }
}

private class FakeRenderer(
    private val support: FailureReason?,
) : CustomFormRenderer {
    override fun deviceSupport() = support

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
