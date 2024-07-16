package org.dhis2.maps.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dhis2.maps.model.MapItemModel

@Composable
fun MapScreen(
    items: List<MapItemModel>,
    listState: LazyListState = rememberLazyListState(),
    onItemScrolled: (item: MapItemModel) -> Unit,
    onNavigate: (item: MapItemModel) -> Unit,
    actionButtons: @Composable (ColumnScope.() -> Unit),
    map: @Composable (BoxScope.() -> Unit),
    onItem: @Composable LazyItemScope.(item: MapItemModel) -> Unit,

) {
    Box(modifier = Modifier.fillMaxSize()) {
        map()
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        ) {
            actionButtons()
        }
        MapItemHorizontalPager(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = listState,
            items = items,
            onItem = onItem,
            onItemScrolled = onItemScrolled,
            onNavigate = onNavigate,
        )
    }
}
