package org.dhis2.usescases.programEventDetail.eventList

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.filters.WorkingListFilter

interface EventListFragmentView {
    fun setLiveData(pagedListLiveData: LiveData<PagedList<EventViewModel>>)
    fun configureWorkingList(workingListFilter: WorkingListFilter?)
}
