package org.dhis2.commons.filters.sorting

import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.ProgramType

class Sorting {

    companion object {
        @JvmStatic
        fun getSortingOptions(programType: ProgramType) = when (programType) {
            ProgramType.EVENT -> eventsSorting()
            ProgramType.TRACKER -> trackerSearchSorting()
            ProgramType.DASHBOARD -> trackerDashboardSorting()
            else -> emptyList()
        }

        private fun trackerSearchSorting(): List<Filters> {
            return arrayListOf(
                Filters.ORG_UNIT,
                Filters.ENROLLMENT_DATE,
                Filters.ENROLLMENT_STATUS,
                Filters.PERIOD,
            )
        }

        private fun trackerDashboardSorting(): List<Filters> {
            return arrayListOf(
                Filters.ORG_UNIT,
                Filters.PERIOD,
            )
        }

        private fun eventsSorting(): List<Filters> {
            return arrayListOf(
                Filters.ORG_UNIT,
                Filters.PERIOD,
            )
        }
    }
}
