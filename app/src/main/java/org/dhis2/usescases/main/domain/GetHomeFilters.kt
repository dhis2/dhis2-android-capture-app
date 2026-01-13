package org.dhis2.usescases.main.domain

import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError

class GetHomeFilters(
    private val filterRepository: FilterRepository,
) : UseCase<Unit, List<FilterItem>> {
    override suspend fun invoke(input: Unit): Result<List<FilterItem>> =
        try {
            Result.success(filterRepository.homeFilters())
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
