package org.dhis2.tracker.search.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.tracker.search.data.SearchTrackedEntityRepository
import org.dhis2.tracker.search.model.SearchTrackedEntitiesInput
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult

class SearchTrackedEntities(
    private val repository: SearchTrackedEntityRepository,
    private val customIntentRepository: CustomIntentRepository,
    private val teType: String,
) : UseCase<SearchTrackedEntitiesInput, Flow<PagingData<TrackedEntitySearchItemResult>>> {
    override suspend fun invoke(input: SearchTrackedEntitiesInput): Result<Flow<PagingData<TrackedEntitySearchItemResult>>> {
        try {
            prepareQuery(input, teType)
            // Due to performance issues and possible duplicates we exclude the values in the exclude list only when no program is selected
            if (input.excludeValues?.isNotEmpty() == true && input.selectedProgram == null) {
                repository.excludeValuesFromQuery(input.excludeValues.toList())
            }
            // fetch results from repository based on the prepared query
            // and if the device is online and has state filters
            return Result.success(
                repository.fetchResults(
                    isOnline = input.isOnline,
                    hasStateFilters = input.hasStateFilters,
                    allowCache = input.allowCache,
                ),
            )
        } catch (domainError: DomainError) {
            // Errors will be handled by the view layer
            return Result.failure(domainError)
        }
    }

    /**
     * Invoke method for immediate non-paginated results.
     * Useful for scenarios like QR code scanning where immediate results are needed.
     */
    suspend fun invokeImmediate(input: SearchTrackedEntitiesInput): Result<List<TrackedEntitySearchItemResult>> {
        try {
            prepareQuery(input, teType)
            // Due to performance issues and possible duplicates we exclude the values in the exclude list only when no program is selected
            if (input.excludeValues?.isNotEmpty() == true && input.selectedProgram == null) {
                repository.excludeValuesFromQuery(input.excludeValues.toList())
            }
            // fetch immediate results from repository
            return Result.success(
                repository.fetchImmediateResults(
                    isOnline = input.isOnline,
                    hasStateFilters = input.hasStateFilters,
                ),
            )
        } catch (domainError: DomainError) {
            return Result.failure(domainError)
        }
    }

    private suspend fun prepareQuery(
        input: SearchTrackedEntitiesInput,
        teType: String,
    ) {
        // Add filters from FilterManager
        repository.addFiltersToQuery(input.selectedProgram, teType)

        input.queryData?.let { queryData ->
            // iterate through the query data and add to the repository query
            for ((dataId, dataValues) in queryData) {
                // checks if the dataId is an attribute of the teType
                val isTETypeAttribute = getIsTETypeAttribute(input.selectedProgram, dataId, teType)

                if (input.selectedProgram != null || isTETypeAttribute) {
                    // fetches the teAttribute details (isUnique, isOptionSet)

                    dataValues?.let { values ->
                        // normalize values: if the attribute doesn't have custom intent that returns multiple values
                        // and there are multiple values, join them into a single comma-separated value
                        val normalizedValues =
                            if (values.size > 1 &&
                                !customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                                    dataId,
                                    CustomIntentActionTypeModel.SEARCH,
                                )
                            ) {
                                mutableListOf(values.joinToString(","))
                            } else {
                                values
                            }

                        val teAttribute = repository.getTEAttribute(dataId)

                        repository.addToQuery(
                            dataId,
                            normalizedValues,
                            teAttribute.isUnique,
                            teAttribute.isOptionSet,
                        )
                    }
                }
            }
        }
    }

    private suspend fun getIsTETypeAttribute(
        selectedProgram: String?,
        dataId: String,
        teType: String,
    ): Boolean =
        if (selectedProgram == null) {
            repository.isTETypeAttribute(teType, dataId)
        } else {
            false
        }
}
