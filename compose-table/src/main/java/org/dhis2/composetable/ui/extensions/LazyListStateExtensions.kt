package org.dhis2.composetable.ui.extensions

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState

suspend fun LazyListState.animateScrollToVisibleItems() {
    animateScrollBy(layoutInfo.viewportSize.height / 2f)
}
