package org.dhis2.usescases.searchTrackEntity

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.usescases.searchTrackEntity.ui.WrappedSearchButton

@ExperimentalAnimationApi
fun ComposeView?.setLandscapeOpenSearchButton(
    searchTEIViewModel: SearchTEIViewModel,
    onClick: () -> Unit,
) {
    this?.setContent {
        MdcTheme {
            val screenState by searchTEIViewModel.screenState.observeAsState()
            val visible = screenState?.let {
                (it is SearchList) && it.searchFilters.isOpened
            } ?: false
            val isLandscape =
                LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
            AnimatedVisibility(visible = isLandscape && visible) {
                WrappedSearchButton(onClick = onClick)
            }
        }
    }
}
