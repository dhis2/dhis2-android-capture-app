package org.dhis2.utils.filters

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.BR

class FilterHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(filterItem: FilterItem) {
        binding.apply {
            setVariable(BR.filterItem, filterItem)
            setVariable(
                BR.workingListScope,
                FilterManager.getInstance().observeWorkingListScope()
            )
            executePendingBindings()
        }
    }
}
