package org.dhis2.data.forms.dataentry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.BR
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.form.model.SectionUiModelImpl

const val NO_POSITION = -1

class DataEntryHeaderHelper(
    private val headerContainer: ViewGroup,
    private val recyclerView: RecyclerView
) {
    private val currentSection = MutableLiveData<SectionUiModelImpl>()

    fun observeHeaderChanges(owner: LifecycleOwner) {
        currentSection.observe(
            owner,
            Observer { section: SectionUiModelImpl? ->
                this.loadHeader(
                    section
                )
            }
        )
    }

    fun checkSectionHeader(recyclerView: RecyclerView) {
        val dataEntryAdapter = recyclerView.adapter as DataEntryAdapter
        val visiblePos = when (recyclerView.layoutManager) {
            is GridLayoutManager ->
                (recyclerView.layoutManager as GridLayoutManager?)
                    ?.findFirstVisibleItemPosition()
            else ->
                (recyclerView.layoutManager as LinearLayoutManager?)
                    ?.findFirstVisibleItemPosition()
        } ?: NO_POSITION

        if (visiblePos != NO_POSITION && dataEntryAdapter.getSectionSize() > 1) {
            dataEntryAdapter.getSectionForPosition(visiblePos)?.let { headerSection ->
                if (headerSection.isOpen && !dataEntryAdapter.isSection(visiblePos + 1)) {
                    if (currentSection.value == null ||
                        currentSection.value!!.uid != headerSection.uid
                    ) {
                        currentSection.value = headerSection
                    }
                } else {
                    currentSection.setValue(null)
                }
            }
        }
    }

    private fun loadHeader(section: SectionUiModelImpl?) {
        val dataEntryAdapter = recyclerView.adapter as DataEntryAdapter
        if (section != null && section.isOpen) {
            val layoutInflater = LayoutInflater.from(headerContainer.context)
            val binding =
                DataBindingUtil.inflate<ViewDataBinding>(
                    layoutInflater,
                    section.layoutId,
                    headerContainer,
                    false
                )
            val sectionHolder = FormViewHolder(binding)
            val sectionPosition: Int? = dataEntryAdapter.getSectionPosition(section.uid)
            sectionPosition?.let {
                dataEntryAdapter.updateSectionData(it, true)
            }
            headerContainer.removeAllViews()
            headerContainer.addView(sectionHolder.itemView)
            binding.setVariable(BR.item, section)
        } else {
            headerContainer.removeAllViews()
        }
    }

    fun onItemsUpdatedCallback() {
        val dataEntryAdapter = recyclerView.adapter as DataEntryAdapter
        currentSection.value?.let { section ->
            dataEntryAdapter.getSectionPosition(section.uid)?.let {
                loadHeader(dataEntryAdapter.getSectionForPosition(it))
            }
        } ?: run {
            if (dataEntryAdapter.getSectionSize() > 1) {
                loadHeader(dataEntryAdapter.getSectionForPosition(0))
            }
        }
    }
}
