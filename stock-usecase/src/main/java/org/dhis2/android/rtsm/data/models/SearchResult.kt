package org.dhis2.android.rtsm.data.models

import androidx.lifecycle.LiveData

data class SearchResult(
    val items: LiveData<List<StockItem>>,
)
