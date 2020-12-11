package org.dhis2.utils.filters

import androidx.annotation.DrawableRes
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import org.dhis2.R
import org.dhis2.utils.filters.sorting.Sorting
import org.dhis2.utils.filters.sorting.SortingItem
import org.dhis2.utils.filters.sorting.SortingItem.Companion.create
import org.dhis2.utils.filters.sorting.SortingStatus
import org.dhis2.utils.filters.workingLists.WorkingListItem
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod

sealed class FilterItem(
    val type: Filters,
    open val programType: ProgramType,
    open val sortingItem: ObservableField<SortingItem>,
    open val openFilter: ObservableField<Filters>,
    open val filterLabel: String
) {
    open fun filterValue(): ObservableField<String> {
        return ObservableField("Hello there!")
    }

    @DrawableRes
    abstract fun icon(): Int

    fun showSorting(): Boolean {
        return Sorting.getSortingOptions(programType).any { it == type }
    }

    fun observeCount(): ObservableField<Int> {
        return FilterManager.getInstance().observeField(type)
    }

    fun onSortingClick() {
        if (!FilterManager.getInstance().isFilterActiveForWorkingList(type)) {
            val sortItem = create(type)
            if (sortingItem.get() != null &&
                sortingItem.get()?.filterSelectedForSorting == sortItem.filterSelectedForSorting
            ) {
                when {
                    sortingItem.get()?.sortingStatus === SortingStatus.ASC -> {
                        sortItem.sortingStatus = SortingStatus.DESC
                    }
                    sortingItem.get()?.sortingStatus === SortingStatus.DESC -> {
                        sortItem.sortingStatus = SortingStatus.NONE
                    }
                    else -> {
                        sortItem.sortingStatus = SortingStatus.ASC
                    }
                }
            } else {
                sortItem.sortingStatus = SortingStatus.ASC
            }
            sortingItem.set(sortItem)
            FilterManager.getInstance().sortingItem = sortingItem.get()
        }
    }

    fun onClick() {
        if (!FilterManager.getInstance().isFilterActiveForWorkingList(type)) {
            openFilter.set(if (openFilter.get() != type) type else null)
        } else {
            openFilter.set(null)
        }
    }

    fun displayExpandArrow(): Boolean {
        return type != Filters.ASSIGNED_TO_ME
    }
}

data class PeriodFilter(
    val selectedPeriodId: Int,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String
) : FilterItem(Filters.PERIOD, programType, sortingItem, openFilter, filterLabel) {
    private val currentValue = ObservableField("No filters applied")
    override fun filterValue(): ObservableField<String> {
        return currentValue
    }

    fun setSelectedPeriod(periods: List<DatePeriod>, checkedId: Int) {
        if (checkedId != FilterManager.getInstance().periodIdSelected) {
            FilterManager.getInstance().periodIdSelected = checkedId
            FilterManager.getInstance().addPeriod(periods)
            currentValue.set(if (periods.isNotEmpty()) "Filters applying" else "No filters applied")
        }
    }

    fun requestPeriod(periodRequest: FilterManager.PeriodRequest, checkedId: Int) {
        FilterManager.getInstance().addPeriodRequest(periodRequest, Filters.PERIOD)
        if (checkedId != FilterManager.getInstance().periodIdSelected) {
            FilterManager.getInstance().periodIdSelected = checkedId
        }
    }

    override fun icon(): Int {
        return R.drawable.ic_calendar_positive
    }
}

data class EnrollmentDateFilter(
    val selectedEnrollmentPeriodId: Int,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String
) : FilterItem(Filters.ENROLLMENT_DATE, programType, sortingItem, openFilter, filterLabel) {
    fun setSelectedPeriod(periods: List<DatePeriod>, checkedId: Int) {
        if (checkedId != FilterManager.getInstance().enrollmentPeriodIdSelected) {
            FilterManager.getInstance().enrollmentPeriodIdSelected = checkedId
            FilterManager.getInstance().addEnrollmentPeriod(periods)
        }
    }

    fun requestPeriod(periodRequest: FilterManager.PeriodRequest, checkedId: Int) {
        FilterManager.getInstance().addPeriodRequest(periodRequest, Filters.ENROLLMENT_DATE)
        if (checkedId != FilterManager.getInstance().enrollmentPeriodIdSelected) {
            FilterManager.getInstance().enrollmentPeriodIdSelected = checkedId
        }
    }

    override fun icon(): Int {
        return R.drawable.ic_calendar_positive
    }
}

data class EnrollmentStatusFilter(
    var selectedEnrollmentStatus: List<EnrollmentStatus>?,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String
) : FilterItem(Filters.ENROLLMENT_STATUS, programType, sortingItem, openFilter, filterLabel) {

    fun setEnrollmentStatus(addEnrollment: Boolean, enrollmentStatus: EnrollmentStatus) {
        FilterManager.getInstance().addEnrollmentStatus(!addEnrollment, enrollmentStatus)
    }

    fun observeEnrollmentStatus(): ObservableField<EnrollmentStatus> {
        return FilterManager.getInstance().observeEnrollmentStatus()
    }

    override fun icon(): Int {
        return R.drawable.ic_enrollment_status_filter
    }
}

data class OrgUnitFilter(
    var selectedOrgUnits: LiveData<List<OrganisationUnit>>,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String
) :
    FilterItem(Filters.ORG_UNIT, programType, sortingItem, openFilter, filterLabel) {
    override fun icon(): Int {
        return R.drawable.ic_filter_ou
    }
}

data class SyncStateFilter(
    var selectedSyncStates: List<State>,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String
) : FilterItem(Filters.SYNC_STATE, programType, sortingItem, openFilter, filterLabel) {
    fun setSyncStatus(addState: Boolean, vararg syncStates: State) {
        FilterManager.getInstance().addState(!addState, *syncStates)
    }

    fun observeSyncState(): ObservableField<List<State>> {
        return FilterManager.getInstance().observeSyncState()
    }

    override fun icon(): Int {
        return R.drawable.ic_filter_sync
    }
}

data class CatOptionComboFilter(
    val catCombo: CategoryCombo,
    val catOptionCombos: List<CategoryOptionCombo>,
    var selectedCatOptCombos: List<CategoryOptionCombo>,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String
) : FilterItem(Filters.CAT_OPT_COMB, programType, sortingItem, openFilter, filterLabel) {
    fun showSpinner(): Boolean {
        return catOptionCombos.size < 15
    }

    fun showDialog() {
        FilterManager.getInstance().addCatOptComboRequest(catCombo.uid())
    }

    fun selectCatOptionCombo(position: Int) {
        FilterManager.getInstance().addCatOptCombo(catOptionCombos[position])
    }

    override fun icon(): Int {
        return R.drawable.ic_filter_sync
    }
}

data class EventStatusFilter(
    var selectedEventStatus: List<EventStatus>,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String
) :
    FilterItem(Filters.EVENT_STATUS, programType, sortingItem, openFilter, filterLabel) {
    fun setEventStatus(addStatus: Boolean, vararg eventStatus: EventStatus) {
        FilterManager.getInstance().addEventStatus(!addStatus, *eventStatus)
    }

    fun observeEventStatus(): ObservableField<List<EventStatus>> {
        return FilterManager.getInstance().observeEventStatus()
    }

    override fun icon(): Int {
        return R.drawable.ic_status
    }
}

data class AssignedFilter(
    val assignedToMe: Boolean? = null,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String
) : FilterItem(Filters.ASSIGNED_TO_ME, programType, sortingItem, openFilter, filterLabel) {
    fun activate(setActive: Boolean) {
        FilterManager.getInstance().setAssignedToMe(setActive)
    }

    fun observeAssignedToMe(): ObservableField<Boolean> {
        return FilterManager.getInstance().observeAssignedToMe()
    }

    override fun icon(): Int {
        return R.drawable.ic_assignment
    }
}

data class WorkingListFilter(
    val workingLists: List<WorkingListItem>,
    var currentWorkingList: WorkingListItem?,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String
) : FilterItem(Filters.WORKING_LIST, programType, sortingItem, openFilter, filterLabel) {
    fun onChecked(checkedId: Int) {
        workingLists.firstOrNull { it.hashCode() == checkedId }?.let {
            if (!it.isSelected()) {
                it.select()
            }
        } ?: workingLists.forEach {
            it.deselect()
        }.also {
            FilterManager.getInstance().currentWorkingList(null)
        }
    }

    override fun icon(): Int {
        return -1
    }
}
