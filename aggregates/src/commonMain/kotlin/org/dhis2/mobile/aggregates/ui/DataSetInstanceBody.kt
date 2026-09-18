package org.dhis2.mobile.aggregates.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters

/**
 * The body of the data set instance screen — the panes, section tabs and table — when something
 * other than this module draws it. Everything framing it stays the host's: top bar, save button,
 * bottom bar and snackbar.
 *
 * @param contentPadding space the host's chrome occupies over the body, today the save button.
 * @param onHostRefresh re-reads the details, completion status and editability the host shows. The
 * host does not observe writes made outside it.
 */
fun interface DataSetInstanceBody {
    @Composable
    fun Content(
        contentPadding: PaddingValues,
        onHostRefresh: () -> Unit,
    )
}

/**
 * Whether anything replaces the host's own body for a given instance.
 *
 * Bound in `:app` when a plugin can claim a data set, and resolved optionally: nothing binds it in
 * this module, and an unbound provider means every data set keeps the default table.
 *
 * Not `@Composable`: the same answer decides whether the view model builds tables at all, and one
 * pure function asked with the same parameters is what keeps the two from disagreeing.
 */
fun interface DataSetInstanceBodyProvider {
    fun bodyFor(parameters: DataSetInstanceParameters): DataSetInstanceBody?
}
