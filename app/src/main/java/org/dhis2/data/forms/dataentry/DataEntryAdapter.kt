package org.dhis2.data.forms.dataentry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import java.util.ArrayList
import java.util.LinkedHashMap
import org.dhis2.data.forms.dataentry.fields.FieldUiModel
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.data.forms.dataentry.fields.FormViewHolder.FieldItemCallback
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel

class DataEntryAdapter :
    ListAdapter<FieldUiModel, FormViewHolder>(DataEntryDiff()),
    FieldItemCallback {
    private val sectionHandler = SectionHandler()
    private val currentFocusUid: MutableLiveData<String> = MutableLiveData()
    private var lastFocusItem: String? = null
    private var nextFocusPosition = -1
    var sectionPositions: MutableMap<String, Int> = LinkedHashMap()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)
        return FormViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        if (getItem(position) is SectionViewModel) {
            updateSectionData(position, false)
        }
        holder.bind(getItem(position), position, this)
    }

    fun updateSectionData(position: Int, isHeader: Boolean) {
        (getItem(position) as SectionViewModel?)!!.setShowBottomShadow(
            !isHeader && position > 0 && getItem(
                position - 1
            ) !is SectionViewModel
        )
        (getItem(position) as SectionViewModel?)!!.sectionNumber = getSectionNumber(position)
        (getItem(position) as SectionViewModel?)!!.setLastSectionHeight(
            position > 0 && position == itemCount - 1 && getItem(
                position - 1
            ) !is SectionViewModel
        )
    }

    private fun getSectionNumber(sectionPosition: Int): Int {
        var sectionNumber = 1
        for (i in 0 until sectionPosition) {
            if (getItem(i) is SectionViewModel) {
                sectionNumber++
            }
        }
        return sectionNumber
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)!!.getLayoutId()
    }

    fun swap(updates: List<FieldViewModel>, commitCallback: Runnable) {
        sectionPositions = LinkedHashMap()
        val items: MutableList<FieldUiModel> = ArrayList()
        for (fieldViewModel in updates) {
            if (fieldViewModel is SectionViewModel) {
                sectionPositions[fieldViewModel.getUid()] = updates.indexOf(fieldViewModel)
            }
            items.add(fieldViewModel)
        }
        submitList(items) {
            var currentFocusPosition = -1
            var lastFocusPosition = -1
            if (lastFocusItem != null) {
                nextFocusPosition = -1
                for (i in items.indices) {
                    val item = items[i] as FieldViewModel
                    if (item.getUid() == lastFocusItem) {
                        lastFocusPosition = i
                        nextFocusPosition = i + 1
                    }
                    if (i == nextFocusPosition && !item.editable()!! && item !is SectionViewModel) {
                        nextFocusPosition++
                    }
                    if (item.getUid() == currentFocusUid.value) currentFocusPosition = i
                }
            }
            if (nextFocusPosition != -1 && currentFocusPosition == lastFocusPosition && nextFocusPosition < items.size) currentFocusUid.setValue(
                getItem(nextFocusPosition)!!.getUid()
            ) else if (currentFocusPosition != -1 && currentFocusPosition < items.size) currentFocusUid.setValue(
                getItem(currentFocusPosition)!!.getUid()
            )
            commitCallback.run()
        }
    }

    fun setLastFocusItem(lastFocusItem: String) {
        currentFocusUid.value = lastFocusItem
        nextFocusPosition = -1
        this.lastFocusItem = lastFocusItem
    }

    fun getSectionSize(): Int {
        return sectionPositions.size
    }

    fun getSectionForPosition(visiblePos: Int): SectionViewModel? {
        val sectionPosition = sectionHandler.getSectionPositionFromVisiblePosition(
            visiblePos,
            isSection(visiblePos),
            ArrayList(sectionPositions.values)
        )
        return if (sectionPosition != -1) {
            getItem(sectionPosition) as SectionViewModel?
        } else {
            null
        }
    }

    fun getSectionPosition(sectionUid: String): Int? {
        return sectionPositions[sectionUid]
    }

    fun isSection(position: Int): Boolean {
        return if (position <= itemCount) {
            getItemViewType(position) == DataEntryViewHolderTypes.SECTION.ordinal
        } else {
            false
        }
    }

    override fun onNext(position: Int) {
        if (position < itemCount) {
            getItem(position + 1)!!.onActivate()
        }
    }
}
