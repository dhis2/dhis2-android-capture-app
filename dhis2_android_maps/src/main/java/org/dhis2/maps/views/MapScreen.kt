package org.dhis2.maps.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dhis2.maps.R
import org.dhis2.maps.location.LocationState
import org.dhis2.maps.model.MapItemModel
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

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
    var pagerMaxHeight by remember { mutableStateOf(Dp.Unspecified) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    pagerMaxHeight = (it.size.height * 0.7).dp
                },
    ) {
        map()
        Column(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
        ) {
            actionButtons()
        }
        MapItemHorizontalPager(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .testTag("MAP_CAROUSEL")
                    .heightIn(max = pagerMaxHeight),
            state = listState,
            items = items,
            onItem = onItem,
            onItemScrolled = onItemScrolled,
            onNavigate = onNavigate,
        )
    }
}

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    actionButtons: @Composable (ColumnScope.() -> Unit),
    map: @Composable (BoxScope.() -> Unit),
    extraContent: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        map()
        Column(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
        ) {
            actionButtons()
        }
        extraContent()
    }
}

@Composable
fun LocationIcon(
    locationState: LocationState,
    onLocationButtonClicked: () -> Unit,
) {
    IconButton(
        style = IconButtonStyle.TONAL,
        icon = {
            Icon(
                painter =
                    painterResource(
                        when (locationState) {
                            LocationState.FIXED -> R.drawable.ic_gps_fixed
                            LocationState.NOT_FIXED -> R.drawable.ic_gps_not_fixed
                            LocationState.OFF -> R.drawable.ic_gps_off
                        },
                    ),
                contentDescription = "location",
                tint = TextColor.OnPrimaryContainer,
            )
        },
        onClick = onLocationButtonClicked,
    )
}
