package org.dhis2.mobile.aggregates.ui.viewModel

import androidx.lifecycle.ViewModel
import org.dhis2.mobile.aggregates.data.DataFetcher
import org.dhis2.mobile.aggregates.ui.states.previewDataSetScreenState

class DataSetTableViewModel(
    private val dataFetcher: DataFetcher,
) : ViewModel() {
    fun fetchState(useTwoPane: Boolean) = previewDataSetScreenState(
        useTwoPane = useTwoPane,
        numberOfTabs = 3,
        test = dataFetcher.test(),
    )
}
