package org.dhis2.commons.filters.workingLists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.dhis2.commons.filters.WorkingListFilter
import org.dhis2.commons.filters.data.FilterRepository

class WorkingListViewModel(
    programUid: String?,
    filterRepository: FilterRepository,
) : ViewModel() {

    private val _workingListFilter = MutableLiveData<WorkingListFilter?>()
    val workingListFilter: LiveData<WorkingListFilter?> = _workingListFilter

    init {
        programUid?.let {
            _workingListFilter.postValue(filterRepository.workingListFilter(it))
        }
    }
}
