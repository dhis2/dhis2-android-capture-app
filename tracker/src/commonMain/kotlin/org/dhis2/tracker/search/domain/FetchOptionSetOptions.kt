package org.dhis2.tracker.search.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.tracker.search.data.OptionSetRepository
import org.dhis2.tracker.ui.input.model.TrackerOptionItem

/**
 * Use case for fetching option set options with pagination.
 * Coordinates between repository and UI layer.
 */
class FetchOptionSetOptions(
    private val optionSetRepository: OptionSetRepository,
) : UseCase<FetchOptionSetOptions.Params, Flow<PagingData<TrackerOptionItem>>> {
    override suspend fun invoke(input: Params): Result<Flow<PagingData<TrackerOptionItem>>> =
        try {
            val flow =
                optionSetRepository.getOptions(
                    optionSetUid = input.optionSetUid,
                    pageSize = input.pageSize,
                    searchQuery = input.searchQuery,
                )
            Result.success(flow)
        } catch (e: DomainError) {
            Result.failure(e)
        }

    data class Params(
        val optionSetUid: String,
        val pageSize: Int = 10,
        val searchQuery: String? = null,
    )
}
