package org.dhis2.maps.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.maps.R
import org.dhis2.maps.model.MapItemModel
import org.dhis2.ui.avatar.AvatarProvider
import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.FAB
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MapItemHorizontalPager(
    modifier: Modifier = Modifier,
    state: LazyListState,
    items: List<MapItemModel>,
    onItemScrolled: (item: MapItemModel) -> Unit,
    onNavigate: (item: MapItemModel) -> Unit,
    onItem: @Composable (LazyItemScope.(item: MapItemModel) -> Unit),
) {
    var currentExpandedItem: Int? by remember {
        mutableStateOf(null)
    }

    var currentItem: MapItemModel? by remember(state, items) {
        mutableStateOf(items.firstOrNull())
    }

    LaunchedEffect(key1 = state.isScrollInProgress) {
        if (state.isScrollInProgress) {
            currentExpandedItem = null
            currentItem = null
        } else {
            currentItem = items.getOrNull(state.firstVisibleItemIndex)
            currentItem?.let {
                onItemScrolled(it)
            }
        }
    }

    Column(modifier = modifier) {
        AnimatedVisibility(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .align(alignment = Alignment.End),
            visible = !state.isScrollInProgress && items.isNotEmpty(),
        ) {
            FAB(
                onClick = { currentItem?.let { onNavigate(it) } },
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_map_navigate),
                    contentDescription = "",
                    tint = Color.White,
                )
            }
        }
        LazyRow(
            modifier = modifier,
            state = state,
            horizontalArrangement = spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = state),
        ) {
            items(items) { mapItemModel: MapItemModel ->
                onItem(mapItemModel)
            }
        }
    }
}

@Preview(device = "id:pixel_8")
@Composable
fun MapItemHorizontalListPreview() {
    val items = mutableListOf<MapItemModel>()
    repeat(200) { index ->
        items.add(
            MapItemModel(
                uid = UUID.randomUUID().toString(),
                avatarProviderConfiguration = AvatarProviderConfiguration.ProfilePic(
                    "",
                    "Tem",
                ),
                title = "Title",
                description = null,
                lastUpdated = "5 min ago",
                additionalInfoList = listOf(
                    AdditionalInfoItem(key = "key:", value = "Hello there"),
                    AdditionalInfoItem(key = "key:", value = "Hello there"),
                    AdditionalInfoItem(key = "key:", value = "Hello there"),
                    AdditionalInfoItem(key = "key:", value = "Hello there"),
                    AdditionalInfoItem(key = "key:", value = "Hello there"),
                    AdditionalInfoItem(key = "key:", value = "Hello there"),
                    AdditionalInfoItem(key = "key:", value = "Hello there"),
                    AdditionalInfoItem(key = "key:", value = "Hello there"),
                ),
                isOnline = false,
                geometry = null,
                relatedInfo = null,
                state = State.SYNCED,
            ),
        )
    }

    Scaffold { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            MapItemHorizontalPager(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                state = rememberLazyListState(),
                items = items,
                onItemScrolled = {},
                onNavigate = {},
            ) { item ->

                ListCard(
                    modifier = Modifier.fillParentMaxWidth(),
                    listAvatar = {
                        AvatarProvider(
                            avatarProviderConfiguration = item.avatarProviderConfiguration,
                            onImageClick = {
                            },
                        )
                    },
                    title = ListCardTitleModel(
                        text = item.title,
                    ),
                    additionalInfoList = item.additionalInfoList,
                    onCardClick = {
                    },
                )
            }
        }
    }
}
