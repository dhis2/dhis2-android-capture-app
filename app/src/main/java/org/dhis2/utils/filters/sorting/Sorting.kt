package org.dhis2.utils.filters.sorting

import org.dhis2.utils.filters.Filters
import org.dhis2.utils.filters.FiltersAdapter

class Sorting {

    companion object {
        @JvmStatic
        fun getSortingOptions(programType: FiltersAdapter.ProgramType) =
            when (programType) {
                FiltersAdapter.ProgramType.EVENT -> eventsSorting()
                FiltersAdapter.ProgramType.TRACKER -> trackerSearchSorting()
                FiltersAdapter.ProgramType.DASHBOARD -> trackerDashboardSorting()
                else -> emptyList()
            }

        private fun trackerSearchSorting(): List<Filters> {
            return arrayListOf(
                Filters.ORG_UNIT,
                Filters.ENROLLMENT_DATE,
                Filters.ENROLLMENT_STATUS,
                Filters.PERIOD
            )
        }

        private fun trackerDashboardSorting(): List<Filters> {
            return arrayListOf(
                Filters.ORG_UNIT,
                Filters.PERIOD
            )
        }

        private fun eventsSorting(): List<Filters> {
            return arrayListOf(
                Filters.ORG_UNIT,
                Filters.PERIOD
            )
        }
    }
}
