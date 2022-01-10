package org.dhis2.data.forms.dataentry

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.data.forms.dataentry.fields.FormViewHolder.FieldItemCallback
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.ui.DataEntryDiff
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent

class DataEntryAdapter(private val searchStyle: Boolean) :
    ListAdapter<FieldUiModel, FormViewHolder>(DataEntryDiff()),
    FieldItemCallback {

    private val refactoredViews = intArrayOf(
        R.layout.form_age_custom,
        R.layout.form_date_time,
        R.layout.form_scan,
        R.layout.form_org_unit,
        R.layout.form_coordinate_custom,
        R.layout.form_edit_text_custom
    )

    var onIntent: ((intent: FormIntent) -> Unit)? = null
    var onRecyclerViewUiEvents: ((uiEvent: RecyclerViewUiEvents) -> Unit)? = null

    private val sectionHandler = SectionHandler()
    var sectionPositions: MutableMap<String, Int> = LinkedHashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        val layoutInflater =
            if (refactoredViews.contains(viewType) && searchStyle) {
                LayoutInflater.from(
                    ContextThemeWrapper(
                        parent.context,
                        R.style.searchFormInputText
                    )
                )
            } else {
                LayoutInflater.from(parent.context)
            }
        val binding =
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)
        return FormViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        if (getItem(position) is SectionUiModelImpl) {
            updateSectionData(position, false)
        }
        holder.bind(getItem(position), this)
    }

    fun updateSectionData(position: Int, isHeader: Boolean) {
        (getItem(position) as SectionUiModelImpl?)!!.setShowBottomShadow(
            !isHeader && position > 0 && getItem(
                position - 1
            ) !is SectionUiModelImpl
        )
        (getItem(position) as SectionUiModelImpl?)!!.setSectionNumber(getSectionNumber(position))
        (getItem(position) as SectionUiModelImpl?)!!.setLastSectionHeight(
            position > 0 && position == itemCount - 1 && getItem(
                position - 1
            ) !is SectionUiModelImpl
        )
    }

    private fun getSectionNumber(sectionPosition: Int): Int {
        var sectionNumber = 1
        for (i in 0 until sectionPosition) {
            if (getItem(i) is SectionUiModelImpl) {
                sectionNumber++
            }
        }
        return sectionNumber
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)!!.layoutId
    }

    fun swap(updates: List<FieldUiModel>, commitCallback: Runnable) {
        sectionPositions = LinkedHashMap()
        for (fieldViewModel in updates) {
            if (fieldViewModel is SectionUiModelImpl) {
                sectionPositions[fieldViewModel.uid] = updates.indexOf(fieldViewModel)
            }
        }

        submitList(updates) {
            commitCallback.run()
        }
    }

    fun getSectionSize(): Int {
        return sectionPositions.size
    }

    fun getSectionForPosition(visiblePos: Int): SectionUiModelImpl? {
        val sectionPosition = sectionHandler.getSectionPositionFromVisiblePosition(
            visiblePos,
            isSection(visiblePos),
            ArrayList(sectionPositions.values)
        )
        val model = if (sectionPosition != -1) {
            getItem(sectionPosition)
        } else {
            null
        }

        return if (model is SectionUiModelImpl) {
            model
        } else {
            null
        }
    }

    fun getSectionPosition(sectionUid: String): Int? {
        return sectionPositions[sectionUid]
    }

    fun isSection(position: Int): Boolean {
        return if (position < itemCount) {
            getItemViewType(position) == DataEntryViewHolderTypes.SECTION.ordinal
        } else {
            false
        }
    }

    override fun intent(intent: FormIntent) {
        onIntent?.let {
            it(intent)
        }
    }

    override fun recyclerViewEvent(uiEvent: RecyclerViewUiEvents) {
        onRecyclerViewUiEvents?.let {
            it(uiEvent)
        }
    }
}
