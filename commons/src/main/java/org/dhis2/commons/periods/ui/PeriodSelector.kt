package org.dhis2.commons.periods.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import org.dhis2.commons.R
import org.dhis2.commons.periods.model.Period
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing.Spacing8
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import java.util.Date

@Composable
fun PeriodSelectorContent(
    periods: LazyPagingItems<Period>,
    scrollState: LazyListState,
    onPeriodSelected: (Date) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = scrollState,
    ) {
        when (periods.loadState.refresh) {
            is LoadState.Error -> periods.retry()
            LoadState.Loading ->
                item { ProgressItem(contentPadding = PaddingValues(Spacing8)) }

            is LoadState.NotLoading ->
                if (periods.itemCount == 0) {
                    item {
                        ListItem(
                            contentPadding = PaddingValues(Spacing8),
                            label = stringResource(R.string.no_periods),
                            selected = false,
                            enabled = false,
                            onItemClick = {},
                        )
                    }
                } else {
                    items(periods.itemCount) { index ->
                        val period = periods[index]
                        ListItem(
                            contentPadding = PaddingValues(Spacing8),
                            label = period?.name ?: "",
                            selected = period?.selected == true,
                            enabled = period?.enabled == true,
                        ) {
                            period?.startDate?.let(onPeriodSelected)
                        }
                    }
                }
        }
    }
}

@Deprecated("Expose design system item", replaceWith = ReplaceWith("DropDownItem"))
@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onItemClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing8))
            .clickable(enabled = enabled, onClick = onItemClick)
            .background(
                color = if (selected) {
                    SurfaceColor.PrimaryContainer
                } else {
                    Color.Unspecified
                },
            )
            .padding(contentPadding),
    ) {
        Text(
            text = label,
            style = if (selected) {
                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (enabled) TextColor.OnSurface else TextColor.OnDisabledSurface,
        )
    }
}

@Composable
fun ProgressItem(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing8))
            .background(color = Color.Unspecified)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        ProgressIndicator(type = ProgressIndicatorType.CIRCULAR_SMALL)
    }
}
