package org.dhis2.tracker.search

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.tracker.search.data.FilterRepository
import org.dhis2.tracker.search.data.LoadSearchResultsRepository
import org.dhis2.tracker.search.model.SearchTrackerParameterResult
import org.dhis2.tracker.search.model.SearchTrackerParametersModel

class LoadSearchResultsUseCase(
    private val repository: LoadSearchResultsRepository,
    private val customIntentRepository: CustomIntentRepository,
    private val filterRepository: FilterRepository,
    ): UseCase<SearchTrackerParametersModel, Flow<PagingData<SearchTrackerParameterResult>>>   {
    override suspend fun invoke(input: SearchTrackerParametersModel): Result<Flow<PagingData<SearchTrackerParameterResult>>> {

        try {

            Result.success()
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }

    }

}