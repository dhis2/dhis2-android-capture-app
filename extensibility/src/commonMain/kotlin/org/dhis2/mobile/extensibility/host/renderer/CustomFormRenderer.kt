package org.dhis2.mobile.extensibility.host.renderer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.mobile.extensibility.host.model.DataSetInstance
import org.dhis2.mobile.extensibility.host.model.FailureReason

/**
 * What runs a custom form. The WebView implementation lives in `extension-web`; `extension-host`
 * only knows this contract, so it can be tested with a fake.
 *
 * `saveValue` is the only way to write: it is the data set screen's own save, and returns null
 * when saved or the message to show.
 *
 * `onFailed` is for a form that does not finish loading or whose WebView crashes. A script error
 * after the form has loaded is reported by the renderer and the form stays.
 */
interface CustomFormRenderer {
    fun deviceSupported(): Boolean

    @Composable
    fun Render(
        html: String,
        instance: DataSetInstance,
        modifier: Modifier,
        contentPadding: PaddingValues,
        editable: Boolean,
        reloadKey: Int,
        saveValue: suspend (dataElementUid: String, categoryOptionComboUid: String, value: String?) -> String?,
        onFailed: (FailureReason) -> Unit,
    )
}
