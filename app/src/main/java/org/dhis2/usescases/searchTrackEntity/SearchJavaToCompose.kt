package org.dhis2.usescases.searchTrackEntity

import androidx.compose.ui.platform.ComposeView
import com.google.android.material.composethemeadapter.MdcTheme

fun ComposeView.setMinAttributesMessage(searchConfiguration: SearchForm) {
    setContent {
        MdcTheme {
            if (searchConfiguration.minAttributesToSearch > 0) {
                MinAttributesMessage(minAttributes = searchConfiguration.minAttributesToSearch)
            }
        }
    }
}
