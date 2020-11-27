package org.dhis2.utils.filters

import android.os.Handler
import android.view.LayoutInflater
import androidx.databinding.ObservableField
import org.dhis2.databinding.ItemFilterWorkingListBinding
import org.dhis2.databinding.ItemFilterWorkingListChipBinding
import org.dhis2.utils.filters.workingLists.WorkingListItem

internal class WorkingListFilterHolder(
    private val mBinding: ItemFilterWorkingListBinding,
    openedFilter: ObservableField<Filters>,
    programType: FiltersAdapter.ProgramType,
    private val workingLists: MutableList<WorkingListItem>,
    private val onAction: () -> Unit
) : FilterHolder(mBinding, openedFilter) {

    init {
        filterType = Filters.WORKING_LIST
        this.programType = programType
    }

    public override fun bind() {
        super.bind()
        mBinding.chipGroup.apply {
            removeAllViews()
            workingLists.forEach { workingListItem ->
                addView(
                    ItemFilterWorkingListChipBinding.inflate(
                        LayoutInflater.from(context),
                        this,
                        false
                    ).apply {
                        tag = workingListItem.uid
                        workingList = workingListItem
                        chip.id = workingListItem.hashCode()
                        chip.isChecked = workingListItem.isSelected()
                    }.root
                )
            }
            setOnCheckedChangeListener { group, checkedId ->
                workingLists.firstOrNull { it.hashCode() == checkedId }?.let {
                    if(!it.isSelected()) {
                        it.select()
                    }
                    Handler().post { onAction() }
                }?:FilterManager.getInstance().currentWorkingList(null)
            }
        }
    }
}
