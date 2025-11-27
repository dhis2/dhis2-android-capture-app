package org.dhis2.commons.periods.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import org.dhis2.commons.R
import org.dhis2.commons.periods.model.Period
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownListItem
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing.Spacing8

@Composable
fun PeriodSelectorContent(
    periods: LazyPagingItems<Period>,
    scrollState: LazyListState,
    onPeriodSelected: (Period) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .testTag("period_selector")
                .fillMaxWidth()
                .animateContentSize(),
        state = scrollState,
    ) {
        when (periods.loadState.refresh) {
            is LoadState.Error -> periods.retry()
            LoadState.Loading ->
                item { ProgressItem(contentPadding = PaddingValues(Spacing8)) }

            is LoadState.NotLoading ->
                if (periods.itemCount == 0) {
                    item {
                        DropdownListItem(
                            item =
                                DropdownItem(
                                    label = stringResource(R.string.no_periods),
                                ),
                            contentPadding = PaddingValues(Spacing8),
                            selected = false,
                            enabled = false,
                            onItemClick = {},
                        )
                    }
                } else {
                    items(periods.itemCount) { index ->
                        val period = periods[index]
                        DropdownListItem(
                            modifier = Modifier.testTag("period_item_$index"),
                            item =
                                DropdownItem(
                                    label = period?.name ?: "",
                                ),
                            contentPadding = PaddingValues(Spacing8),
                            selected = period?.selected == true,
                            enabled = period?.enabled == true,
                        ) {
                            period?.let {
                                onPeriodSelected(it)
                            }
                        }
                    }
                }
        }
    }
}

@Composable
fun ProgressItem(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(384.dp)
                .clip(RoundedCornerShape(Spacing8))
                .background(color = Color.Unspecified)
                .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        ProgressIndicator(type = ProgressIndicatorType.CIRCULAR_SMALL)
    }
}
