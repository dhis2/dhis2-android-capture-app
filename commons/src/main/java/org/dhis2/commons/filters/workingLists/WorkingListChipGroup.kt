package org.dhis2.commons.filters.workingLists

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.databinding.Observable
import com.google.android.material.chip.ChipGroup
import org.dhis2.commons.databinding.ItemFilterWorkingListChipBinding
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.WorkingListFilter
import org.dhis2.commons.filters.data.EmptyWorkingList

class WorkingListChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ChipGroup(context, attrs, defStyleAttr) {

    fun setWorkingLists(workingLists: List<WorkingListItem>) {
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
                    chip.id = workingListItem.id()
                    chip.isChecked = workingListItem.isSelected()
                }.root
            )
        }
    }

    fun setWorkingFilter(workingListFilter: WorkingListFilter) {
        workingListFilter.observeScope()
            .addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    if (FilterManager.getInstance().observeWorkingListScope()
                        .get() is EmptyWorkingList && checkedChipId != -1
                    ) {
                        clearCheck()
                    }
                }
            })
    }
}
