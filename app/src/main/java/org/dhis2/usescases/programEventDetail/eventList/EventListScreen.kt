package org.dhis2.usescases.programEventDetail.eventList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.dhis2.R
import org.dhis2.commons.filters.workingLists.WorkingListChipGroup
import org.dhis2.commons.filters.workingLists.WorkingListViewModel
import org.dhis2.commons.ui.ListCardProvider
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

@Composable
fun EventListScreen(
    eventListViewModel: EventListViewModel,
    workingListViewModel: WorkingListViewModel,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Absolute.spacedBy(Spacing.Spacing4),
    ) {
        WorkingListChipGroup(
            Modifier.padding(top = Spacing.Spacing16),
            workingListViewModel,
        )
        val events = eventListViewModel.eventList.collectAsLazyPagingItems()
        when (events.loadState.refresh) {
            is LoadState.Error -> {
                // no-op
            }

            LoadState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
                }
            }

            is LoadState.NotLoading -> {
                if (events.itemCount < 1) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 42.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.empty_tei_add),
                        )
                    }
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        verticalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                    ) {
                        items(count = events.itemCount) { index ->
                            ListCardProvider(
                                card = events[index]!!,
                                syncingResourceId = R.string.syncing,
                            )

                            if (index == events.itemCount - 1) {
                                Spacer(modifier = Modifier.padding(100.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
