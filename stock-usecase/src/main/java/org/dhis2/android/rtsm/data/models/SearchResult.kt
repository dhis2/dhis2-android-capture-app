package org.dhis2.android.rtsm.data.models

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class SearchResult(
    val items: LiveData<PagedList<StockItem>>,
    val totalCount: Int
)
