package org.dhis2.usescases.programEventDetail.eventList

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import org.dhis2.commons.data.EventViewModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel
import org.hisp.dhis.android.core.dataelement.DataElement

interface EventListFragmentView {
    fun setLiveData(pagedListLiveData: LiveData<PagedList<EventViewModel>>)
    fun setTextTypeDataElementsFilter(textTypeDataElementsFilter: List<DataElement>)
}
