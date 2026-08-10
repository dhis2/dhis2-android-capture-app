package org.dhis2.usescases.programEventDetail.eventList

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.ui.model.ListCardUiModel

data class EventListState(
    val eventList: Flow<PagingData<ListCardUiModel>>,
    val customEventLabel: String,
)
