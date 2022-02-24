package org.dhis2.usescases.searchTrackEntity

import android.content.res.Configuration
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import com.google.android.material.composethemeadapter.MdcTheme

fun ComposeView.setMinAttributesMessage(minAttributesToSearch: Int) {
    setContent {
        MdcTheme {
            if (minAttributesToSearch > 0) {
                MinAttributesMessage(minAttributes = minAttributesToSearch)
            }
        }
    }
}

fun ComposeView?.setLandscapeOpenSearchButton(onClick: () -> Unit) {
    this?.setContent {
        MdcTheme {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                WrappedSearchButton(onClick = onClick)
            }
        }
    }
}
