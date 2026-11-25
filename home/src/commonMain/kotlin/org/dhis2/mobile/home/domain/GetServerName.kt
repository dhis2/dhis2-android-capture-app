package org.dhis2.mobile.home.domain

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.home.data.HomeRepository

/**
 * Returns the name of the server of the current account (its Application title, or its URL when it
 * has none), or an empty string when there is no current account.
 */
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
