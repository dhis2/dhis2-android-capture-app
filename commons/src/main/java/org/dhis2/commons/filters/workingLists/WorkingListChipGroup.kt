package org.dhis2.commons.filters.workingLists

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.HorizontalScrollView
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.databinding.Observable
import com.google.android.material.chip.ChipGroup
import org.dhis2.commons.bindings.scrollToPosition
import org.dhis2.commons.databinding.ItemFilterWorkingListChipBinding
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.WorkingListFilter
import org.dhis2.commons.filters.data.EmptyWorkingList
import org.hisp.dhis.mobile.ui.designsystem.component.Chip
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

class WorkingListChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ChipGroup(context, attrs, defStyleAttr) {

    private var scrollContainer: HorizontalScrollView? = null

    fun setWorkingLists(workingLists: List<WorkingListItem>) {
        removeAllViews()
        workingLists.forEach { workingListItem ->
            addView(
                ItemFilterWorkingListChipBinding.inflate(
                    LayoutInflater.from(context),
                    this,
                    false,
                ).apply {
                    workingList = workingListItem
                    chip.id = workingListItem.id()
                    chip.isChecked = workingListItem.isSelected()
                    chip.tag = workingListItem.uid
                    chip.setOnCheckedChangeListener { _, checked ->
                        if (checked) {
                            scrollContainer?.scrollToPosition(chip.tag as String)
                        }
                    }
                }.root,
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

    fun setChipScrollContainer(view: HorizontalScrollView) {
        this.scrollContainer = view
    }
}

@Composable
fun WorkingListChipGroup(
    modifier: Modifier = Modifier,
    workingListViewModel: WorkingListViewModel,
) {
    val workingListFilterState = workingListViewModel.workingListFilter.observeAsState()
    var selectedWorkingList by remember {
        mutableStateOf(
            FilterManager.getInstance().currentWorkingList(),
        )
    }
    FilterManager.getInstance().observeWorkingListScope().addOnPropertyChangedCallback(
        object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                selectedWorkingList = FilterManager.getInstance().currentWorkingList()
            }
        },
    )

    workingListFilterState.value?.let { workingListFilter ->
        LazyRow(modifier) {
            itemsIndexed(workingListFilter.workingLists) { index, workingList ->
                Chip(
                    modifier = Modifier.padding(
                        start = if (index == 0) Spacing.Spacing16 else Spacing.Spacing0,
                        end = if (index == workingListFilter.workingLists.size - 1) {
                            Spacing.Spacing16
                        } else {
                            Spacing.Spacing8
                        },
                    ),
                    label = workingList.label,
                    selected = selectedWorkingList?.uid == workingList.uid,
                    onSelected = { _ ->
                        workingListFilter.onChecked(workingList.id())
                    },
                )
            }
        }
    }
}
