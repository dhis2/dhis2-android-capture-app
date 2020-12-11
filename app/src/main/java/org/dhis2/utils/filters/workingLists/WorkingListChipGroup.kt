package org.dhis2.utils.filters.workingLists

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.chip.ChipGroup
import org.dhis2.databinding.ItemFilterWorkingListChipBinding

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
                    chip.id = workingListItem.hashCode()
                    chip.isChecked = workingListItem.isSelected()
                }.root
            )
        }
    }
}
