package org.dhis2.mobile.aggregates.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.aggregates.model.Violation
import org.dhis2.mobile.aggregates.resources.Res
import org.dhis2.mobile.aggregates.resources.validation_rules_data_to_review
import org.hisp.dhis.mobile.ui.designsystem.component.Tag
import org.hisp.dhis.mobile.ui.designsystem.component.TagType
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max

@Composable
internal fun ValidationRulesErrorDialog(violations: List<Violation>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(Spacing.Spacing24),
    ) {
        val pageCount = violations.size
        val pagerState =
            rememberPagerState(
                pageCount = { pageCount },
            )

        if (pageCount > 1) {
            PagerIndicator(
                pageCount = pageCount,
                currentPage = pagerState.currentPage,
            )
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = Spacing.Spacing8,
            contentPadding = PaddingValues(horizontal = Spacing.Spacing24),
        ) { page ->
            val violation = violations[page]

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(Shape.Large)
                        .background(SurfaceColor.PrimaryContainer)
                        .padding(Spacing.Spacing16),
            ) {
                item {
                    Column(
                        verticalArrangement = spacedBy(Spacing.Spacing8),
                    ) {
                        Text(
                            text = violation.description ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = violation.instruction ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.Spacing24))
                }
                item {
                    Text(
                        text = stringResource(Res.string.validation_rules_data_to_review),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(violation.dataToReview) { dataToReview ->
                    Spacer(modifier = Modifier.height(Spacing.Spacing8))
                    Row(
                        horizontalArrangement = spacedBy(Spacing.Spacing4),
                    ) {
                        Text(
                            text = "${dataToReview.formattedDataLabel()}:",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Tag(
                            label = dataToReview.value,
                            type = TagType.ERROR,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PagerIndicator(
    indicatorScrollState: LazyListState = rememberLazyListState(),
    pageCount: Int,
    currentPage: Int,
    dotIndicatorColor: Color = SurfaceColor.Error,
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
                .width(120.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { iteration ->
            val color =
                if (currentPage == iteration) dotIndicatorColor else SurfaceColor.ErrorContainer
            item(key = "item$iteration") {
                val firstVisibleIndex by remember { derivedStateOf { indicatorScrollState.firstVisibleItemIndex } }
                val lastVisibleIndex =
                    indicatorScrollState.layoutInfo.visibleItemsInfo
                        .lastOrNull()
                        ?.index ?: 0
                val size by animateDpAsState(
                    targetValue =
                        when (iteration) {
                            currentPage -> Spacing.Spacing14
                            in firstVisibleIndex + 1..<lastVisibleIndex -> 10.dp
                            else -> Spacing.Spacing14
                        },
                    label = "PagerIndicatorDotSizeAnimation",
                )
                Box(
                    modifier =
                        Modifier
                            .padding(all = Spacing.Spacing8)
                            .background(color = color, CircleShape)
                            .size(
                                size,
                            ),
                )
            }
        }
    }
}
