package org.dhis2.mobile.aggregates.ui.body

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters

/**
 * Seam: whether something other than the native tables draws a data set instance's body.
 *
 * Declared here and bound in `:app`, so `aggregates` never depends on the extension modules.
 * Optional: when nothing is bound every data set keeps its native form.
 *
 * Suspending because answering it reads the SDK (the custom form). The view model asks it once,
 * on its IO dispatcher, before building any table, so it never builds tables it won't show.
 */
fun interface DataSetInstanceBodyProvider {
    /** Null keeps the native tables. */
    suspend fun bodyFor(parameters: DataSetInstanceParameters): DataSetInstanceBody?
}

/**
 * The replaced body. The host keeps everything around it: top bar, sync, save/complete FAB,
 * validation and the non-editable/reopen bar.
 */
fun interface DataSetInstanceBody {
    /**
     * @param contentPadding room the host's chrome (the FAB) takes over the body.
     * @param editable decided only by the host (completed, no write access, expired…).
     * @param reloadKey changes when the host knows values changed outside the body (sync,
     * reopen); the body re-reads its values.
     * @param saveValue the only way the body writes: the same save a table cell uses, so
     * validation and error messages are the same. Returns null when saved, otherwise the message
     * to show. The host refuses it when the instance is not editable.
     * @param onFailed the body cannot render: it does not finish loading, or its WebView
     * crashes. The host switches to the native form and tells the user; the body has already
     * reported the reason. A script error after the body has loaded is only reported by the
     * body: it stays on screen and does not call this.
     */
    @Composable
    fun Content(
        modifier: Modifier,
        contentPadding: PaddingValues,
        editable: Boolean,
        reloadKey: Int,
        saveValue: SaveBodyValue,
        onFailed: () -> Unit,
    )
}

typealias SaveBodyValue = suspend (dataElementUid: String, categoryOptionComboUid: String, value: String?) -> String?
