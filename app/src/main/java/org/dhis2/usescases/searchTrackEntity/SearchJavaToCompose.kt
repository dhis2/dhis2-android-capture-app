package org.dhis2.usescases.searchTrackEntity

import android.content.res.Configuration
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.usescases.searchTrackEntity.ui.WrappedSearchButton

fun ComposeView?.setLandscapeOpenSearchButton(onClick: () -> Unit) {
    this?.setContent {
        MdcTheme {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                WrappedSearchButton(onClick = onClick)
            }
        }
    }
}
