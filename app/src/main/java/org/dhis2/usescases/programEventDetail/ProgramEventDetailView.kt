package org.dhis2.usescases.programEventDetail

import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager.PeriodRequest
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.program.Program

interface ProgramEventDetailView : AbstractActivityContracts.View {
    fun setProgram(programModel: Program)
    fun renderError(message: String)
    fun showHideFilter()
    fun setWritePermission(canWrite: Boolean)
    fun showFilterProgress()
    fun updateFilters(totalFilters: Int)
    fun openOrgUnitTreeSelector()
    fun showPeriodRequest(periodRequest: PeriodRequest)
    fun startNewEvent()
    fun navigateToEvent(eventId: String, orgUnit: String)
    fun showSyncDialog(uid: String)
    fun showCatOptComboDialog(catComboUid: String)
    fun setFilterItems(programFilters: List<FilterItem>)
    fun hideFilters()
}
