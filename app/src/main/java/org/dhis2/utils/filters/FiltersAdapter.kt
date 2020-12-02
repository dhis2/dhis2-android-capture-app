package org.dhis2.utils.filters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.data.filter.FilterPresenter
import org.dhis2.databinding.ItemFilterAssignedBinding
import org.dhis2.databinding.ItemFilterCatOptCombBinding
import org.dhis2.databinding.ItemFilterEnrollmentStatusBinding
import org.dhis2.databinding.ItemFilterOrgUnitBinding
import org.dhis2.databinding.ItemFilterPeriodBinding
import org.dhis2.databinding.ItemFilterStateBinding
import org.dhis2.databinding.ItemFilterStatusBinding
import org.dhis2.databinding.ItemFilterWorkingListBinding
import org.dhis2.utils.filters.sorting.SortingItem

class FiltersAdapter(
    val programType: ProgramType,
    val filterPresenter: FilterPresenter
) : ListAdapter<FilterItem, FilterHolder>(object : DiffUtil.ItemCallback<FilterItem>() {
    override fun areItemsTheSame(oldItem: FilterItem, newItem: FilterItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FilterItem, newItem: FilterItem): Boolean {
        return oldItem == newItem
    }
}) {
    private val openedFilter: ObservableField<Filters> = ObservableField()
    private val sortingItem: ObservableField<SortingItem> = ObservableField()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FilterHolder {
        val inflater =
            LayoutInflater.from(parent.context)
        return when (Filters.values()[viewType]) {
            Filters.PERIOD -> PeriodFilterHolder(
                ItemFilterPeriodBinding.inflate(
                    inflater,
                    parent,
                    false
                ), openedFilter, sortingItem, programType
            )
            Filters.ENROLLMENT_DATE -> EnrollmentDateFilterHolder(
                ItemFilterPeriodBinding.inflate(
                    inflater,
                    parent,
                    false
                ), openedFilter, sortingItem, programType
            )
            Filters.ORG_UNIT -> OrgUnitFilterHolder(
                ItemFilterOrgUnitBinding.inflate(
                    inflater,
                    parent,
                    false
                ), openedFilter, sortingItem, programType, filterPresenter
            )
            Filters.SYNC_STATE -> SyncStateFilterHolder(
                ItemFilterStateBinding.inflate(
                    inflater,
                    parent,
                    false
                ), openedFilter, sortingItem, programType
            )
            Filters.CAT_OPT_COMB -> CatOptCombFilterHolder(
                ItemFilterCatOptCombBinding.inflate(
                    inflater,
                    parent,
                    false
                ), openedFilter, null, programType
            )
            Filters.EVENT_STATUS -> StatusEventFilterHolder(
                ItemFilterStatusBinding.inflate(
                    inflater,
                    parent,
                    false
                ), openedFilter, programType
            )
            Filters.ASSIGNED_TO_ME -> AssignToMeFilterHolder(
                ItemFilterAssignedBinding.inflate(
                    inflater,
                    parent,
                    false
                ), openedFilter, programType
            )
            Filters.ENROLLMENT_STATUS -> StatusEnrollmentFilterHolder(
                ItemFilterEnrollmentStatusBinding.inflate(
                    inflater,
                    parent,
                    false
                ),
                openedFilter,
                sortingItem,
                programType
            )
            Filters.WORKING_LIST -> WorkingListFilterHolder(
                ItemFilterWorkingListBinding.inflate(
                    inflater,
                    parent,
                    false
                ), openedFilter, programType
            )
        }
    }

    override fun onBindViewHolder(holder: FilterHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }
}