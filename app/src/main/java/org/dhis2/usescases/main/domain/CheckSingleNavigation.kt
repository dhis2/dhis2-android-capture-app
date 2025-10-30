package org.dhis2.usescases.main.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.HomeItemData
import org.dhis2.usescases.main.data.HomeRepository

class CheckSingleNavigation(
    private val homeRepository: HomeRepository,
) : UseCase<Unit, HomeItemData> {
    override suspend fun invoke(input: Unit): Result<HomeItemData> =
        try {
            val homeItemCount = homeRepository.homeItemCount()
            if (homeItemCount == 1) {
                Result.success(homeRepository.singleHomeItemData())
            } else {
                Result.failure(Exception("No programs"))
            }
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
