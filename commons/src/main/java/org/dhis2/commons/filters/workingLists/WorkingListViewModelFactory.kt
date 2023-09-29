package org.dhis2.commons.filters.workingLists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.filters.data.FilterRepository

class WorkingListViewModelFactory(
    private val programUid: String,
    private val filterRepository: FilterRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkingListViewModel(
            programUid,
            filterRepository,
        ) as T
    }
}
