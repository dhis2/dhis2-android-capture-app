package org.dhis2.mobile.home.domain

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.home.data.HomeRepository

class GetServerName(
    private val homeRepository: HomeRepository,
) : UseCase<Unit, String> {
    override suspend fun invoke(input: Unit): Result<String> =
        try {
            Result.success(homeRepository.serverName())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
