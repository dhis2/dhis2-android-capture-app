package org.dhis2.utils.filters

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.ObservableField
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.ItemFilterSortingBinding
import org.dhis2.utils.filters.sorting.Sorting
import org.dhis2.utils.filters.sorting.SortingFilterAdapter
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

internal class SortingFilterHolder(
    binding: ItemFilterSortingBinding,
    programType: FiltersAdapter.ProgramType?,
    openedFilter: ObservableField<Filters?>?
) : FilterHolder(binding, openedFilter) {
    private var currentOrgUnit: OrganisationUnit? = null
    private val filters: List<Sorting> = when (programType) {
        FiltersAdapter.ProgramType.EVENT -> eventsSortings()
        FiltersAdapter.ProgramType.TRACKER -> trackerSearchSortings()
        FiltersAdapter.ProgramType.DASHBOARD -> trackerDashboardSortings()
        else -> emptyList()
    }

    public override fun bind() {
        super.bind()
        filterIcon.setImageDrawable(
            AppCompatResources.getDrawable(
                itemView.context,
                R.drawable.ic_sort_descending
            )
        )
        filterTitle.setText(R.string.filters_title_sorting)
        setUpAdapter()
    }

    private fun setUpAdapter() {
        val d2 =
            (itemView.context.applicationContext as App).serverComponent()!!.userManager()
                .d2
        val localBinding =
            binding as ItemFilterSortingBinding
        val sortingFilterAdapter = SortingFilterAdapter()
        localBinding.filterSorting.sortingRecycler.adapter = sortingFilterAdapter
        localBinding.filterSorting
            .sortingSearchEditText
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {}

                override fun onTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                    if (charSequence.length > 3) {
                        currentOrgUnit = d2.organisationUnitModule().organisationUnits()
                            .byDisplayName().like("%$charSequence%").one().blockingGet()
                        if (currentOrgUnit != null) {
                            localBinding.filterSorting.sortingtHint.text =
                                currentOrgUnit!!.displayName()
                        } else {
                            localBinding.filterSorting.sortingtHint.text = null
                        }
                    } else {
                        localBinding.filterSorting.sortingtHint.text = null
                    }
                }

                override fun afterTextChanged(editable: Editable) {}
            })

        localBinding.filterSorting.addButton.setOnClickListener { view: View? ->
            if (currentOrgUnit != null) {
                FilterManager.getInstance().addOrgUnit(currentOrgUnit)
                currentOrgUnit = null
                localBinding.filterSorting.sortingSearchEditText.text = null
                localBinding.filterSorting.sortingtHint.text = null
                sortingFilterAdapter.notifyDataSetChanged()
            }
        }

        localBinding.filterSorting.sortingTreeButton.setOnClickListener {
            localBinding.root.clearFocus()
            FilterManager.getInstance().ouTreeProcessor.onNext(true)
        }
    }

    fun trackerSearchSortings(): List<Sorting> {
        return Sorting.values().toList()
    }

    fun trackerDashboardSortings(): List<Sorting> {
        return arrayListOf(
            Sorting.CREATION_DATE,
            Sorting.LAST_UPDATED,
            Sorting.ORG_UNIT_NAME,
            Sorting.EVENT_DATE,
            Sorting.COMPLETE_DATE
        )
    }

    fun eventsSortings(): List<Sorting> {
        return arrayListOf(
            Sorting.CREATION_DATE,
            Sorting.LAST_UPDATED,
            Sorting.ORG_UNIT_NAME,
            Sorting.EVENT_DATE,
            Sorting.COMPLETE_DATE
        )
    }

    fun dataSetSortings(): List<Sorting> {
        return arrayListOf(
            Sorting.CREATION_DATE,
            Sorting.LAST_UPDATED,
            Sorting.ORG_UNIT_NAME,
            Sorting.COMPLETE_DATE
        )
    }
}
