package org.dhis2.utils.filters.ou

import android.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import org.dhis2.data.filter.FilterPresenter
import org.dhis2.databinding.FilterOrgUnitBinding
import org.dhis2.utils.filters.Filters
import org.dhis2.utils.filters.OrgUnitFilter
import org.dhis2.utils.filters.sorting.FilteredOrgUnitResult
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OrgUnitFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ouFilterAdapter by lazy { OUFilterAdapter() }
    private var filterPresenter: FilterPresenter? = null
    private val binding =
        FilterOrgUnitBinding.inflate(LayoutInflater.from(context), this, true).apply {
            ouRecycler.adapter = ouFilterAdapter
            orgUnitSearchEditText.apply {
                dropDownVerticalOffset = 4
                addTextChangedListener(
                    { _, _, _, _ -> progress.visibility = View.VISIBLE },
                    { charSequence, _, _, _ ->
                        val filteredOrgUnitResult: FilteredOrgUnitResult? =
                            filterPresenter?.getOrgUnitsByName(charSequence.toString())
                        if (filteredOrgUnitResult?.hasResult() == true) {
                            val autoCompleteAdapter =
                                ArrayAdapter(
                                    context,
                                    R.layout.simple_dropdown_item_1line,
                                    filteredOrgUnitResult.names()
                                )
                            orgUnitSearchEditText.setAdapter(
                                autoCompleteAdapter
                            )
                            orgUnitSearchEditText.showDropDown()
                        }
                    },
                    { progress.visibility = View.GONE }
                )
            }
            addButton.setOnClickListener {
                filterPresenter?.addOrgUnitToFilter {
                    orgUnitSearchEditText.text = null
                    ouFilterAdapter.notifyDataSetChanged()
                }
            }
            ouTreeButton.setOnClickListener {
                root.clearFocus()
                filterPresenter?.onOpenOrgUnitTreeSelector()
            }
        }

    fun setFilterItem(filterItem: OrgUnitFilter) {
        filterItem.selectedOrgUnits.observe(
            (context as LifecycleOwner),
            Observer<List<OrganisationUnit?>> { ouFilterAdapter.notifyDataSetChanged() }
        )
    }

    fun setFilterPresenter(filterPresenter: FilterPresenter) {
        this.filterPresenter = filterPresenter
    }

    fun setCurrentFilter(currentFilter: ObservableField<Filters>) {
        binding.currentFilter = currentFilter
    }

    fun setFilterType(filterType: Filters) {
        binding.filterType = filterType
    }
}