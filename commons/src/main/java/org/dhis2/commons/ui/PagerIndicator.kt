package org.dhis2.commons.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PagerIndicatorScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val pageCount = 2
        val pagerState =
            rememberPagerState(
                pageCount = { pageCount },
            )
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .weight(1f),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Page $it")
            }
        }
        PagerIndicator(
            pageCount = pageCount,
            currentPage = pagerState.currentPage,
        )
    }
}

@Composable
fun PagerIndicator(
    indicatorScrollState: LazyListState = rememberLazyListState(),
    pageCount: Int,
    currentPage: Int,
    dotIndicatorColor: Color = Color.DarkGray,
) {
    LaunchedEffect(key1 = currentPage) {
        val size = indicatorScrollState.layoutInfo.visibleItemsInfo.size
        val lastVisibleIndex =
            indicatorScrollState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index ?: 0
        val firstVisibleItemIndex = indicatorScrollState.firstVisibleItemIndex

        if (currentPage > lastVisibleIndex - 1) {
            indicatorScrollState.animateScrollToItem(currentPage - size + 2)
        } else if (currentPage <= firstVisibleItemIndex + 1) {
            indicatorScrollState.animateScrollToItem(max(currentPage - 1, 0))
        }
    }

    LazyRow(
        state = indicatorScrollState,
        modifier =
            Modifier
                .height(50.dp)
                .width(((6 + 16) * 2 + 3 * (10 + 16)).dp),
        // I'm hard computing it to simplify
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { iteration ->
            val color = if (currentPage == iteration) dotIndicatorColor else Color.LightGray
            item(key = "item$iteration") {
                val firstVisibleIndex by remember { derivedStateOf { indicatorScrollState.firstVisibleItemIndex } }
                val lastVisibleIndex =
                    indicatorScrollState.layoutInfo.visibleItemsInfo
                        .lastOrNull()
                        ?.index ?: 0
                val size by animateDpAsState(
                    targetValue =
                        when (iteration) {
                            currentPage -> 10.dp
                            in firstVisibleIndex + 1..<lastVisibleIndex -> 10.dp
                            else -> 6.dp
                        },
                    label = "PagerIndicatorDotSizeAnimation",
                )
                Box(
                    modifier =
                        Modifier
                            .padding(all = 8.dp)
                            .background(color = color, CircleShape)
                            .size(
                                size,
                            ),
                )
            }
        }
    }
}
