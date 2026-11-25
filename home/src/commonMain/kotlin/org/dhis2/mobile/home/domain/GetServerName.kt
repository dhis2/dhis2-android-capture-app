package org.dhis2.mobile.home.domain

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.home.data.HomeRepository

/** Returns the URL of the server of the current account, or an empty string when there is none. */
class GetServerName(
    private val homeRepository: HomeRepository,
) : UseCase<Unit, String> {
    override suspend fun invoke(input: Unit): Result<String> =
        try {
            Result.success(homeRepository.serverName().orEmpty())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            Result.failure(exception)
        }
}
