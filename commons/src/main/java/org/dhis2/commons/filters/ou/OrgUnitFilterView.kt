package org.dhis2.commons.filters.ou

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import org.dhis2.commons.databinding.FilterOrgUnitBinding
import org.dhis2.commons.filters.OrgUnitFilter
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.filters.di.FilterPresenterProvider
import org.dhis2.commons.filters.sorting.FilteredOrgUnitResult
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OrgUnitFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ouFilterAdapter by lazy { OUFilterAdapter() }

    private val filterPresenter: FilterPresenter? by lazy {
        (context.applicationContext as FilterPresenterProvider).provideFilterPresenter()
    }

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
                                    android.R.layout.simple_dropdown_item_1line,
                                    filteredOrgUnitResult.names(),
                                )
                            orgUnitSearchEditText.setAdapter(
                                autoCompleteAdapter,
                            )
                            orgUnitSearchEditText.showDropDown()
                        }
                    },
                    { progress.visibility = View.GONE },
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
        binding.filterItem = filterItem
        binding.filterType = filterItem.type
        filterItem.selectedOrgUnits.observe(
            (context as LifecycleOwner),
            Observer<List<OrganisationUnit?>> { ouFilterAdapter.notifyDataSetChanged() },
        )
    }
}
