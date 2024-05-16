package org.dhis2.commons.filters.data

import org.dhis2.commons.filters.FilterManager
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummaryCollectionRepository
import javax.inject.Inject

class DataSetFilterSearchHelper @Inject constructor(
    private val filterRepository: FilterRepository,
    val filterManager: FilterManager,
) : FilterHelperActions<DataSetInstanceSummaryCollectionRepository> {

    fun getFilteredDataSetSearchRepository(): DataSetInstanceSummaryCollectionRepository {
        return applyFiltersTo(
            filterRepository.dataSetInstanceSummaries(),
        )
    }

    override fun applyFiltersTo(
        repository: DataSetInstanceSummaryCollectionRepository,
    ): DataSetInstanceSummaryCollectionRepository {
        return repository
            .withFilter { applyOrgUnitFilter(it) }
            .withFilter { applyStateFilter(it) }
            .withFilter { applyDateFilter(it) }
            .withFilter { applySorting(it) }
    }

    private fun applyOrgUnitFilter(
        dataSetInstanceRepository: DataSetInstanceSummaryCollectionRepository,
    ): DataSetInstanceSummaryCollectionRepository {
        return if (filterManager.orgUnitUidsFilters.isNotEmpty()) {
            filterRepository.applyOrgUnitFilter(
                dataSetInstanceRepository,
                filterManager.orgUnitUidsFilters,
            )
        } else {
            dataSetInstanceRepository
        }
    }

    private fun applyStateFilter(
        dataSetInstanceRepository: DataSetInstanceSummaryCollectionRepository,
    ): DataSetInstanceSummaryCollectionRepository {
        return if (filterManager.stateFilters.isNotEmpty()) {
            filterRepository.applyStateFilter(dataSetInstanceRepository, filterManager.stateFilters)
        } else {
            dataSetInstanceRepository
        }
    }

    private fun applyDateFilter(
        dataSetInstanceRepository: DataSetInstanceSummaryCollectionRepository,
    ): DataSetInstanceSummaryCollectionRepository {
        return if (filterManager.periodFilters.isNotEmpty()) {
            filterRepository.applyPeriodFilter(
                dataSetInstanceRepository,
                filterManager.periodFilters,
            )
        } else {
            dataSetInstanceRepository
        }
    }

    override fun applySorting(
        repository: DataSetInstanceSummaryCollectionRepository,
    ): DataSetInstanceSummaryCollectionRepository {
        return repository
    }
}
