package org.dhis2.utils.filters

import androidx.databinding.ObservableField
import org.dhis2.databinding.ItemFilterWorkingListBinding

internal class WorkingListFilterHolder(
    mBinding: ItemFilterWorkingListBinding,
    openedFilter: ObservableField<Filters>,
    programType: ProgramType
) : FilterHolder(mBinding, openedFilter) {

    init {
        filterType = Filters.WORKING_LIST
        this.programType = programType
    }
}
