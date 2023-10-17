package org.dhis2.commons.filters

import androidx.annotation.DrawableRes
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import org.dhis2.commons.R
import org.dhis2.commons.filters.data.WorkingListScope
import org.dhis2.commons.filters.sorting.Sorting
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.sorting.SortingItem.Companion.create
import org.dhis2.commons.filters.sorting.SortingStatus
import org.dhis2.commons.filters.workingLists.WorkingListItem
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
    open val filterLabel: String,
) {
    @DrawableRes
    abstract fun icon(): Int

    fun showSorting(): Boolean {
        return Sorting.getSortingOptions(programType).any { it == type }
    }

    fun observeCount(): ObservableField<Int> {
        return FilterManager.getInstance().observeField(type)
    }

    fun getFilterValue(defaultValue: String): String {
        return FilterManager.getInstance().getFilterStringValue(type, defaultValue)
    }

    fun onSortingClick() {
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

    fun onClick() {
        if (!FilterManager.getInstance().isFilterActiveForWorkingList(type)) {
            openFilter.set(if (openFilter.get() != type) type else null)
        } else {
            openFilter.set(null)
        }
    }

    fun displayExpandArrow(): Boolean {
        val filters = listOf(Filters.FOLLOW_UP, Filters.ASSIGNED_TO_ME)
        return !filters.any { it == type }
    }
}

data class PeriodFilter(
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String,
) : FilterItem(Filters.PERIOD, programType, sortingItem, openFilter, filterLabel) {

    fun setSelectedPeriod(periods: List<DatePeriod>, checkedId: Int) {
        if (checkedId != FilterManager.getInstance().periodIdSelected.get()) {
            FilterManager.getInstance().periodIdSelected.set(checkedId)
            FilterManager.getInstance().addPeriod(periods)
        }
    }

    fun requestPeriod(periodRequest: FilterManager.PeriodRequest, checkedId: Int) {
        FilterManager.getInstance().addPeriodRequest(periodRequest, Filters.PERIOD)
        if (checkedId != FilterManager.getInstance().periodIdSelected.get()) {
            FilterManager.getInstance().periodIdSelected.set(checkedId)
        }
    }

    fun observePeriod(): ObservableField<Int> {
        return FilterManager.getInstance().periodIdSelected
    }

    override fun icon(): Int {
        return R.drawable.ic_calendar_positive
    }
}

data class EnrollmentDateFilter(
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String,
) : FilterItem(Filters.ENROLLMENT_DATE, programType, sortingItem, openFilter, filterLabel) {
    fun setSelectedPeriod(periods: List<DatePeriod>, checkedId: Int) {
        if (checkedId != FilterManager.getInstance().enrollmentPeriodIdSelected.get()) {
            FilterManager.getInstance().enrollmentPeriodIdSelected.set(checkedId)
            FilterManager.getInstance().addEnrollmentPeriod(periods)
        }
    }

    fun requestPeriod(periodRequest: FilterManager.PeriodRequest, checkedId: Int) {
        FilterManager.getInstance().addPeriodRequest(periodRequest, Filters.ENROLLMENT_DATE)
        if (checkedId != FilterManager.getInstance().enrollmentPeriodIdSelected.get()) {
            FilterManager.getInstance().enrollmentPeriodIdSelected.set(checkedId)
        }
    }

    override fun icon(): Int {
        return R.drawable.ic_calendar_positive
    }

    fun observePeriod(): ObservableField<Int> {
        return FilterManager.getInstance().enrollmentPeriodIdSelected
    }
}

data class EnrollmentStatusFilter(
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String,
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
    override val filterLabel: String,
) :
    FilterItem(Filters.ORG_UNIT, programType, sortingItem, openFilter, filterLabel) {
    override fun icon(): Int {
        return R.drawable.ic_filter_ou
    }
}

data class SyncStateFilter(
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String,
) : FilterItem(Filters.SYNC_STATE, programType, sortingItem, openFilter, filterLabel) {

    private val syncedCheck = ObservableBoolean(false)
    private val notSyncedCheck = ObservableBoolean(false)
    private val errorCheck = ObservableBoolean(false)
    private val smsCheck = ObservableBoolean(false)

    fun setSyncStatus(addState: Boolean, vararg syncStates: State) {
        FilterManager.getInstance().addState(!addState, *syncStates)
    }

    fun observeSyncState(state: State): ObservableBoolean {
        FilterManager.getInstance().observeSyncState().observeForever {
            syncedCheck.set(it.contains(State.SYNCED))
            notSyncedCheck.set(
                it.contains(State.TO_POST) ||
                    it.contains(State.TO_UPDATE) ||
                    it.contains(State.UPLOADING),
            )
            errorCheck.set(
                it.contains(State.ERROR) ||
                    it.contains(State.WARNING),
            )
            smsCheck.set(
                it.contains(State.SENT_VIA_SMS) ||
                    it.contains(State.SYNCED_VIA_SMS),
            )
        }
        return when (state) {
            State.TO_POST,
            State.TO_UPDATE,
            State.UPLOADING,
            -> notSyncedCheck
            State.RELATIONSHIP,
            State.SYNCED,
            -> syncedCheck
            State.ERROR,
            State.WARNING,
            -> errorCheck
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS,
            -> smsCheck
        }
    }

    override fun icon(): Int {
        return R.drawable.ic_filter_sync
    }
}

data class CatOptionComboFilter(
    val catCombo: CategoryCombo,
    val catOptionCombos: List<CategoryOptionCombo>,
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String,
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
        return R.drawable.ic_category_option_combo_filter
    }
}

data class EventStatusFilter(
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String,
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
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String,
) : FilterItem(Filters.ASSIGNED_TO_ME, programType, sortingItem, openFilter, filterLabel) {
    fun activate(setActive: Boolean) {
        if (!FilterManager.getInstance().isFilterActiveForWorkingList(Filters.ASSIGNED_TO_ME)) {
            FilterManager.getInstance().setAssignedToMe(setActive)
        }
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
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String,
) : FilterItem(Filters.WORKING_LIST, programType, sortingItem, openFilter, filterLabel) {
    fun onChecked(checkedId: Int) {
        openFilter.set(null)
        workingLists.forEach {
            if (it.id() == checkedId) {
                it.select()
            } else {
                it.deselect()
            }
        }.also {
            if (checkedId == -1) {
                FilterManager.getInstance().currentWorkingList(null)
            }
        }
    }

    fun observeScope(): ObservableField<WorkingListScope> {
        return FilterManager.getInstance().observeWorkingListScope()
    }

    override fun icon(): Int {
        return -1
    }
}

data class FollowUpFilter(
    override val programType: ProgramType,
    override val sortingItem: ObservableField<SortingItem>,
    override val openFilter: ObservableField<Filters>,
    override val filterLabel: String,
) : FilterItem(Filters.FOLLOW_UP, programType, sortingItem, openFilter, filterLabel) {
    fun activate(setActive: Boolean) {
        FilterManager.getInstance().setFollowUp(setActive)
    }

    fun observeFollowUp(): ObservableField<Boolean> {
        return FilterManager.getInstance().observeFollowUp()
    }

    override fun icon() = R.drawable.ic_follow_up_filter
}
