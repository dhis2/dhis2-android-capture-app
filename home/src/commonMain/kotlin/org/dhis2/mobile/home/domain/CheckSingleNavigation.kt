package org.dhis2.mobile.home.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.home.data.HomeRepository
import org.dhis2.mobile.home.domain.model.HomeItemData

class CheckSingleNavigation(
    private val homeRepository: HomeRepository,
) : UseCase<Unit, HomeItemData> {
    override suspend fun invoke(input: Unit): Result<HomeItemData> =
        try {
            val homeItemCount = homeRepository.homeItemCount()
            if (homeItemCount == 1) {
                Result.success(homeRepository.singleHomeItemData())
            } else {
                Result.failure(DomainError.ConfigurationError("Expected exactly one home item"))
            }
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
