package org.dhis2.commons.filters.data

import org.dhis2.commons.filters.sorting.SortingStatus
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope

interface FilterHelperActions<T> {
    fun applyFiltersTo(repository: T): T
    fun applySorting(repository: T): T
    fun getSortingDirection(sortingStatus: SortingStatus): RepositoryScope.OrderByDirection? {
        return when (sortingStatus) {
            SortingStatus.NONE -> null
            SortingStatus.ASC -> RepositoryScope.OrderByDirection.ASC
            SortingStatus.DESC -> RepositoryScope.OrderByDirection.DESC
        }
    }

    fun T.withFilter(filterMethod: (T) -> T): T {
        return filterMethod.invoke(this)
    }
}
